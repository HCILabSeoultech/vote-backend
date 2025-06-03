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

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VoteLoadService {

    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final VoteStatisticsUtil voteStatisticsUtil;
    private final VoteSelectRepository voteSelectRepository;

    // 메인페이지 투표 불러오기 (자신이 작성한, 자신이 선택한 카테고리, 자신이 팔로우한 사람의 글)
    public Page<LoadVoteDto> getMainPageVotes(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.USERNAME_NOT_FOUND));

        List<Long> categoryIds = user.getUserInterests().stream()
                .map(interest -> interest.getCategory().getCategoryId())
                .toList();

        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();

        List<Vote> votes = voteRepository.findMainPageVotesUnion(userId, categoryIds, limit, offset);
        long total = voteRepository.countMainPageVotes(userId, categoryIds);

        List<Long> voteIds = votes.stream().map(Vote::getVoteId).toList();
        Map<String, Object> stats = voteStatisticsUtil.collectVoteStatistics(userId, voteIds);

        Page<Vote> votePage = new PageImpl<>(votes, pageable, total);

        return voteStatisticsUtil.getLoadVoteDtos(userId, votePage, stats, pageable);
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
