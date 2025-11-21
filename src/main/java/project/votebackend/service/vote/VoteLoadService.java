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

    // 메인페이지 투표 불러오기
    private static final Long AI_USER_ID = 20L;
    private static final int AI_CADENCE = 3; // 일반 3개마다 AI 1개

    @SuppressWarnings("unchecked")
    public Page<LoadVoteDto> getMainPageVotes(
            Long userId,
            Pageable pageable,
            @Nullable String mixSalt
    ) {

        // 1) 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        List<Long> categoryIds = user.getUserInterests().stream()
                .map(i -> i.getCategory().getCategoryId())
                .toList();

        int pageSize = pageable.getPageSize();
        int fetchSize = pageSize * 30;

        // 2) 후보 전체 가져오기
        List<Vote> candidates = mainPageVoteRepository.findMainFeedUnified(
                userId,
                categoryIds,
                fetchSize,
                AI_USER_ID
        );

        // 3) 참여 여부 & AI 여부 기반 버킷 분리
        List<Vote> nonAiUnselected = new ArrayList<>();
        List<Vote> aiUnselected = new ArrayList<>();
        List<Vote> selected = new ArrayList<>();

        for (Vote v : candidates) {
            boolean participated = voteSelectRepository.existsByUser_UserIdAndVote_VoteId(userId, v.getVoteId());

            if (participated) {
                selected.add(v);    // 참여한 글은 아래로
                continue;
            }

            if (v.getUser().getUserId().equals(AI_USER_ID)) {
                aiUnselected.add(v);   // 미참여 + AI
            } else {
                nonAiUnselected.add(v); // 미참여 + 일반 글
            }
        }

        // 4) 셔플할 그룹만 셔플
        String salt = (mixSalt != null && !mixSalt.isBlank())
                ? mixSalt.trim()
                : LocalDate.now().toString();

        long seed = Objects.hash(userId, salt);
        Random rnd = new Random(seed);

        Collections.shuffle(nonAiUnselected, rnd);
        Collections.shuffle(aiUnselected, rnd);

        // 5) 일반글 사이에 AI 삽입
        List<Vote> mergedTop = new ArrayList<>();
        int i = 0;
        int aiIdx = 0;

        for (Vote v : nonAiUnselected) {
            mergedTop.add(v);
            i++;

            if (i % AI_CADENCE == 0 && aiIdx < aiUnselected.size()) {
                mergedTop.add(aiUnselected.get(aiIdx++));
            }
        }

        // 남은 AI 글이 있으면 뒤에 추가
        while (aiIdx < aiUnselected.size()) {
            mergedTop.add(aiUnselected.get(aiIdx++));
        }

        // 6) 참여한 글은 최신순으로 아래에 붙임
        selected.sort(Comparator.comparing(Vote::getCreatedAt).reversed());

        List<Vote> finalFeed = new ArrayList<>(mergedTop.size() + selected.size());
        finalFeed.addAll(mergedTop);
        finalFeed.addAll(selected);

        // 7) 페이지네이션
        int offset = pageable.getPageNumber() * pageSize;
        int fromIndex = Math.min(offset, finalFeed.size());
        int toIndex = Math.min(offset + pageSize, finalFeed.size());

        List<Vote> paged = finalFeed.subList(fromIndex, toIndex);

        // 8) 통계 계산
        List<Long> voteIds = paged.stream().map(Vote::getVoteId).toList();
        Map<String, Object> stats = voteStatisticsUtil.collectVoteStatistics(userId, voteIds);

        Page<Vote> pageWrapped = new PageImpl<>(paged, pageable, finalFeed.size());
        return voteStatisticsUtil.getLoadVoteDtos(userId, pageWrapped, stats, pageable);
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
