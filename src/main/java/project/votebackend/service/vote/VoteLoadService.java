package project.votebackend.service.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.votebackend.domain.user.User;
import project.votebackend.domain.vote.Vote;
import project.votebackend.dto.vote.LoadVoteDto;
import project.votebackend.exception.AuthException;
import project.votebackend.exception.VoteException;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.repository.vote.VoteRepository;
import project.votebackend.repository.vote.VoteSelectRepository;
import project.votebackend.type.ErrorCode;
import project.votebackend.util.VoteStatisticsUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteLoadService {

    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final VoteStatisticsUtil voteStatisticsUtil;
    private final VoteSelectRepository voteSelectRepository;

    // 메인페이지 투표 불러오기
    public Page<LoadVoteDto> getMainPageVotes(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        List<Long> categoryIds = user.getUserInterests().stream()
                .map(i -> i.getCategory().getCategoryId())
                .toList();

        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int size   = pageable.getPageSize();

        // 기본 피드 (최신순)
        Long aiUserId = 20L;
        List<Vote> base = voteRepository.findMainPageVotesUnion(userId, categoryIds, size, offset, aiUserId);
        long total = voteRepository.countMainPageVotes(userId, categoryIds, aiUserId);

        // 후보 수량 결정 (cadence/ratio는 팀 정책값)
        final int cadence    = 3;         // 3개마다 1개 삽입 시도
        final double aiRatio = 0.5;       // 삽입 지점 중 50%는 AI, 50%는 인기
        int maxInjects = Math.max(1, size / (cadence + 1)); // 대략적인 삽입 수

        // 후보 풀 확보
        List<Vote> ai = voteRepository.findAiCandidatesForCategories(userId, categoryIds, maxInjects * 2, aiUserId);
        List<Vote> pop = voteRepository.findPopularCandidatesForCategories(categoryIds, maxInjects * 2);

        // 머지
        List<Vote> merged = mergeWithDeterministicRandom(
                base, ai, pop, userId, LocalDate.now(), cadence, aiRatio, size
        );

        // 통계 붙여서 DTO 변환
        List<Long> voteIds = merged.stream().map(Vote::getVoteId).toList();
        Map<String, Object> stats = voteStatisticsUtil.collectVoteStatistics(userId, voteIds);

        Page<Vote> pageWrapped = new PageImpl<>(merged, pageable, total /* total은 '기본 피드' 기준 유지 */);
        return voteStatisticsUtil.getLoadVoteDtos(userId, pageWrapped, stats, pageable);
    }

    private List<Vote> mergeWithDeterministicRandom(
            List<Vote> base,
            List<Vote> aiCandidates,
            List<Vote> popCandidates,
            long userId,
            LocalDate today,
            int cadence,
            double aiRatio,
            int pageSize
    ) {
        long seed = Objects.hash(userId, today.toString());
        Random rnd = new Random(seed);

        // 기본에 이미 포함된 후보 제거
        Set<Long> baseIds = base.stream().map(Vote::getVoteId).collect(Collectors.toSet());
        aiCandidates.removeIf(v -> baseIds.contains(v.getVoteId()));
        popCandidates.removeIf(v -> baseIds.contains(v.getVoteId()));

        // 후보를 한 번 셔플
        Collections.shuffle(aiCandidates, rnd);
        Collections.shuffle(popCandidates, rnd);

        List<Vote> out = new ArrayList<>(pageSize);
        Set<Long> used = new HashSet<>();

        int i = 0;
        int b = 0;
        while (out.size() < pageSize && (b < base.size() || !aiCandidates.isEmpty() || !popCandidates.isEmpty())) {
            boolean injectionPoint = cadence > 0 && ((i % (cadence + 1)) == cadence);

            if (injectionPoint) {
                boolean pickAI = rnd.nextDouble() < aiRatio;
                Vote injected = null;
                if (pickAI && !aiCandidates.isEmpty())      injected = aiCandidates.remove(0);
                else if (!popCandidates.isEmpty())          injected = popCandidates.remove(0);
                else if (!aiCandidates.isEmpty())           injected = aiCandidates.remove(0);

                if (injected != null && used.add(injected.getVoteId())) {
                    out.add(injected);
                    i++;
                    continue;
                }
            }

            // 기본 피드 채움
            if (b < base.size()) {
                Vote v = base.get(b++);
                if (used.add(v.getVoteId())) {
                    out.add(v);
                }
                i++;
            } else {
                // 기본이 부족하면 후보로 채움
                Vote fb = !aiCandidates.isEmpty() ? aiCandidates.remove(0)
                        : !popCandidates.isEmpty() ? popCandidates.remove(0)
                        : null;
                if (fb == null) break;
                if (used.add(fb.getVoteId())) out.add(fb);
                i++;
            }
        }
        return out;
    }

    //단일 투표 불러오기
    public LoadVoteDto getVoteById(Long voteId, Long userId) {
        Vote vote = voteRepository.findByIdWithUserAndOptions(voteId)
                .orElseThrow(() -> new VoteException(ErrorCode.VOTE_NOT_FOUND));

        Map<String, Object> stats = voteStatisticsUtil.collectVoteStatistics(userId, List.of(voteId));

        return LoadVoteDto.fromEntityWithAllMaps(
                vote, userId, voteSelectRepository,
                (Map<Long, Integer>) stats.get("optionVoteCountMap"),
                (Map<Long, Integer>) stats.get("commentCountMap"),
                (Map<Long, Integer>) stats.get("likeCountMap"),
                (Map<Long, Boolean>) stats.get("isLikedMap"),
                (Map<Long, Boolean>) stats.get("isBookmarkedMap")
        );
    }

    //특정 카테고리의 글 조회
    public Page<LoadVoteDto> getVotesByCategorySortedByLike(Long categoryId, int page, int size, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);

        // 1. 카테고리별 글을 좋아요 순으로 조회
        Page<Vote> votes = voteRepository.findByCategoryOrderByLikeCount(categoryId, pageable);

        // 2. voteId 목록 추출
        List<Long> voteIds = votes.getContent().stream()
                .map(Vote::getVoteId)
                .toList();

        // 3. 통계 정보 일괄 조회 (DB 조회 최소화)
        Map<String, Object> stats = voteStatisticsUtil.collectVoteStatistics(user.getUserId(), voteIds);

        // 4. 통계 기반 DTO 변환 (성능 최적화)
        return voteStatisticsUtil.getLoadVoteDtos(user.getUserId(), votes, stats, pageable);
    }
}
