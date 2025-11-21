package project.votebackend.service.vote;

import jakarta.annotation.Nullable;
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
import project.votebackend.repository.category.CategoryRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.repository.vote.MainPageVoteRepository;
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
    private final MainPageVoteRepository mainPageVoteRepository;
    private final CategoryRepository categoryRepository;

    // 메인페이지 투표 불러오기
    public Page<LoadVoteDto> getMainPageVotes(Long userId, Pageable pageable, @Nullable String mixSalt) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        List<Long> categoryIds = user.getUserInterests().stream()
                .map(i -> i.getCategory().getCategoryId())
                .toList();

        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int size   = pageable.getPageSize();

        Long aiUserId = 20L;

        // 1) 기본 피드
        List<Vote> base = mainPageVoteRepository.findMainPageVotesUnion(userId, categoryIds, size * 6, 0, aiUserId);

        // 2) base 참여여부 한방 조회 → 분리
        List<Long> baseIds = base.stream().map(Vote::getVoteId).toList();
        Set<Long> basePart = baseIds.isEmpty() ? Set.of()
                : voteSelectRepository.findParticipatedVoteIds(userId, baseIds);

        List<Vote> baseTop = new ArrayList<>(); // 미참여
        List<Vote> baseLow = new ArrayList<>(); // 참여
        for (Vote v : base) {
            if (basePart.contains(v.getVoteId())) baseLow.add(v);
            else baseTop.add(v);
        }

        // 3) 미참여 base만 deterministic 셔플
        String salt = (mixSalt != null && !mixSalt.isBlank()) ? mixSalt.trim() : LocalDate.now().toString();
        long seed = Objects.hash(userId, salt);
        Random rnd = new Random(seed);

        Collections.shuffle(baseTop, rnd);
        Collections.shuffle(baseLow, rnd);

        // 4) AI/인기 기존처럼 top/low 분리
        List<Vote> ai  = mainPageVoteRepository.findAiCandidatesForCategories(userId, categoryIds, size * 4, aiUserId);
        List<Vote> pop = mainPageVoteRepository.findPopularCandidatesGlobal(userId, PageRequest.of(0, size * 4));

        List<Long> aiIds  = ai.stream().map(Vote::getVoteId).toList();
        List<Long> popIds = pop.stream().map(Vote::getVoteId).toList();

        Set<Long> aiPart  = aiIds.isEmpty()  ? Set.of() : voteSelectRepository.findParticipatedVoteIds(userId, aiIds);
        Set<Long> popPart = popIds.isEmpty() ? Set.of() : voteSelectRepository.findParticipatedVoteIds(userId, popIds);

        List<Vote> aiTop = new ArrayList<>(), aiLow = new ArrayList<>();
        for (Vote v : ai)  (aiPart.contains(v.getVoteId())  ? aiLow : aiTop).add(v);
        List<Vote> popTop = new ArrayList<>(), popLow = new ArrayList<>();
        for (Vote v : pop) (popPart.contains(v.getVoteId()) ? popLow : popTop).add(v);

        // 후보 버킷만 셔플
        Collections.shuffle(aiTop, rnd);
        Collections.shuffle(aiLow, rnd);
        Collections.shuffle(popTop, rnd);
        Collections.shuffle(popLow, rnd);

        // 5) 병합: baseTop → baseLow 우선, 3:1 규칙, 주입 시 top → low
        List<Vote> merged = mergeOnlyBaseTopShuffled(
                baseTop, baseLow, aiTop, aiLow, popTop, popLow,
                3, 0.5, size * 4, rnd
        );

        // 6) 최소 300개 보장 — 여기도 top → low 우선 규칙 유지
        int minCount = 300;
        if (merged.size() < minCount) {
            Set<Long> used = merged.stream().map(Vote::getVoteId).collect(Collectors.toSet());

            List<Vote> extraAi  = mainPageVoteRepository.findAiCandidatesForCategories(userId, categoryIds, minCount, aiUserId);
            List<Vote> extraPop = mainPageVoteRepository.findPopularCandidatesGlobal(userId, PageRequest.of(0, minCount));

            // 분리
            Set<Long> extraAiPart  = extraAi.isEmpty()  ? Set.of()
                    : voteSelectRepository.findParticipatedVoteIds(userId, extraAi.stream().map(Vote::getVoteId).toList());
            Set<Long> extraPopPart = extraPop.isEmpty() ? Set.of()
                    : voteSelectRepository.findParticipatedVoteIds(userId, extraPop.stream().map(Vote::getVoteId).toList());

            List<Vote> extraAiTop = new ArrayList<>(), extraAiLow = new ArrayList<>();
            for (Vote v : extraAi) (extraAiPart.contains(v.getVoteId()) ? extraAiLow : extraAiTop).add(v);

            List<Vote> extraPopTop = new ArrayList<>(), extraPopLow = new ArrayList<>();
            for (Vote v : extraPop) (extraPopPart.contains(v.getVoteId()) ? extraPopLow : extraPopTop).add(v);

            // 셔플
            Collections.shuffle(extraAiTop, rnd);
            Collections.shuffle(extraAiLow, rnd);
            Collections.shuffle(extraPopTop, rnd);
            Collections.shuffle(extraPopLow, rnd);

            // top → low 우선으로 채우기
            List<List<Vote>> fillOrder = List.of(extraAiTop, extraPopTop, extraAiLow, extraPopLow);
            outer:
            for (List<Vote> bucket : fillOrder) {
                for (Vote v : bucket) {
                    if (merged.size() >= minCount) break outer;
                    if (used.add(v.getVoteId())) merged.add(v);
                }
            }
        }

        // 1) 사용한 voteId 목록
        Set<Long> usedIds = merged.stream()
                .map(Vote::getVoteId)
                .collect(Collectors.toSet());

        // 2) other categories 구하기
        List<Long> otherCategoryIds = categoryRepository
                .findAllCategoryIdsExcept(categoryIds); // 별도 repository 필요

        if (!otherCategoryIds.isEmpty()) {
            List<Vote> other = mainPageVoteRepository.findOtherCategoryCandidates(
                    otherCategoryIds,
                    minCount * 2,
                    0,
                    aiUserId
            );

            for (Vote v : other) {
                if (usedIds.add(v.getVoteId())) merged.add(v);
                if (merged.size() >= minCount) break;
            }
        }

        // 7) 페이지네이션
        int fromIndex = Math.min(offset, merged.size());
        int toIndex   = Math.min(offset + size, merged.size());
        List<Vote> paged = merged.subList(fromIndex, toIndex);

        // 8) 통계 + PageImpl
        List<Long> voteIds = paged.stream().map(Vote::getVoteId).toList();
        Map<String, Object> stats = voteStatisticsUtil.collectVoteStatistics(userId, voteIds);

        Page<Vote> pageWrapped = new PageImpl<>(paged, pageable, Math.max(merged.size(), minCount));
        return voteStatisticsUtil.getLoadVoteDtos(userId, pageWrapped, stats, pageable);
    }

    private List<Vote> mergeOnlyBaseTopShuffled(
            List<Vote> baseTop,   // 미참여 base
            List<Vote> baseLow,   // 참여 base
            List<Vote> aiTop, List<Vote> aiLow,
            List<Vote> popTop, List<Vote> popLow,
            int cadence, double aiRatio, int pageSize, Random rnd
    ) {
        List<Vote> out = new ArrayList<>(pageSize);
        Set<Long> used = new HashSet<>();
        int i = 0;

        // 주입 후보 선택: 항상 top → low 우선
        java.util.function.Supplier<Vote> pickInjected = () -> {
            boolean pickAI = rnd.nextDouble() < aiRatio;
            Vote v = null;
            if (pickAI) {
                if (!aiTop.isEmpty()) v = aiTop.remove(0);
                else if (!popTop.isEmpty()) v = popTop.remove(0);
                else if (!aiLow.isEmpty()) v = aiLow.remove(0);
                else if (!popLow.isEmpty()) v = popLow.remove(0);
            } else {
                if (!popTop.isEmpty()) v = popTop.remove(0);
                else if (!aiTop.isEmpty()) v = aiTop.remove(0);
                else if (!popLow.isEmpty()) v = popLow.remove(0);
                else if (!aiLow.isEmpty()) v = aiLow.remove(0);
            }
            return v;
        };

        while (out.size() < pageSize &&
                (!baseTop.isEmpty() || !baseLow.isEmpty() || !aiTop.isEmpty() || !aiLow.isEmpty() || !popTop.isEmpty() || !popLow.isEmpty())) {

            boolean injectionPoint = cadence > 0 && ((i % (cadence + 1)) == cadence);

            if (injectionPoint) {
                Vote inj = pickInjected.get();
                if (inj != null && used.add(inj.getVoteId())) {
                    out.add(inj);
                    i++;
                    continue;
                }
            }

            // base 소비: 미참여(baseTop) → 참여(baseLow) 우선
            Vote nextBase = !baseTop.isEmpty() ? baseTop.remove(0)
                    : !baseLow.isEmpty() ? baseLow.remove(0)
                    : null;

            if (nextBase != null) {
                if (used.add(nextBase.getVoteId())) out.add(nextBase);
                i++;
            } else {
                // base가 고갈되면 후보로 백필 (top → low)
                Vote fb = !aiTop.isEmpty() ? aiTop.remove(0)
                        : !popTop.isEmpty() ? popTop.remove(0)
                        : !aiLow.isEmpty() ? aiLow.remove(0)
                        : !popLow.isEmpty() ? popLow.remove(0)
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
