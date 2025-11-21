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
    public Page<LoadVoteDto> getMainPageVotes(
            Long userId,
            Pageable pageable,
            @Nullable String mixSalt
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        List<Long> categoryIds = user.getUserInterests().stream()
                .map(c -> c.getCategory().getCategoryId())
                .toList();

        int pageSize = pageable.getPageSize();
        int fetchSize = pageSize * 30;

        Long aiUserId = 20L;

        // ===============================================
        // 1) 단일 대형 쿼리로 후보 전체 불러오기
        // ===============================================
        List<Vote> candidates = mainPageVoteRepository.findMainFeedUnified(
                userId,
                categoryIds,
                fetchSize,
                aiUserId
        );

        // ===============================================
        // 2) 섞기
        // ===============================================
        String salt = (mixSalt != null && !mixSalt.isBlank())
                ? mixSalt.trim()
                : LocalDate.now().toString();

        long seed = Objects.hash(userId, salt);
        Random rnd = new Random(seed);

        Collections.shuffle(candidates, rnd);

        // ===============================================
        // 3) 페이지네이션
        // ===============================================
        int offset = pageable.getPageNumber() * pageSize;
        int fromIndex = Math.min(offset, candidates.size());
        int toIndex = Math.min(offset + pageSize, candidates.size());

        List<Vote> paged = candidates.subList(fromIndex, toIndex);

        // ===============================================
        // 4) 통계 조회
        // ===============================================
        List<Long> voteIds = paged.stream().map(Vote::getVoteId).toList();
        Map<String, Object> stats = voteStatisticsUtil.collectVoteStatistics(userId, voteIds);

        Page<Vote> pageWrapped = new PageImpl<>(paged, pageable, candidates.size());
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
