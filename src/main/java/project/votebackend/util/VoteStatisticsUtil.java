package project.votebackend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import project.votebackend.domain.Vote;
import project.votebackend.dto.LoadVoteDto;
import project.votebackend.repository.CommentRepository;
import project.votebackend.repository.ReactionRepository;
import project.votebackend.repository.VoteSelectRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VoteStatisticsUtil {

    private final VoteSelectRepository voteSelectRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;

    // 특정 사용자와 여러 투표(voteIds)에 대해 투표 통계 정보를 수집하는 메서드
    public Map<String, Object> collectVoteStatistics(Long userId, List<Long> voteIds) {
        Map<String, Object> result = new HashMap<>();

        // 각 옵션(option)에 대한 투표 수 조회 (voteId → count)
        Map<Long, Integer> optionVoteCountMap = voteSelectRepository.findOptionVoteCounts(voteIds).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(), // voteId
                        row -> ((Number) row[1]).intValue()   // count
                ));

        // 각 투표에 대한 부모 댓글 수 조회 (voteId → count)
        Map<Long, Integer> commentCountMap = commentRepository.countParentCommentsByVoteIds(voteIds).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).intValue()
                ));

        // 좋아요/북마크 반응 정보를 한 번에 조회 (voteId, reactionType, userId)
        List<Object[]> reactionRows = reactionRepository.findReactionsByVoteIds(voteIds);

        // 통계를 담을 맵들 초기화
        Map<Long, Integer> likeCountMap = new HashMap<>();       // voteId → 좋아요 수
        Map<Long, Boolean> isLikedMap = new HashMap<>();         // voteId → 내가 좋아요 했는지
        Map<Long, Boolean> isBookmarkedMap = new HashMap<>();    // voteId → 내가 북마크 했는지

        // 각 반응 정보를 순회하면서 통계 수집
        for (Object[] row : reactionRows) {
            Long voteId = (Long) row[0];
            String reaction = row[1].toString(); // "LIKE" 또는 "BOOKMARK"
            Long reactedUserId = (Long) row[2];

            if (reaction.equals("LIKE")) {
                // 해당 투표의 좋아요 수 증가
                likeCountMap.put(voteId, likeCountMap.getOrDefault(voteId, 0) + 1);
                // 현재 사용자가 좋아요 한 경우 체크
                if (reactedUserId.equals(userId)) {
                    isLikedMap.put(voteId, true);
                }
            }

            if (reaction.equals("BOOKMARK") && reactedUserId.equals(userId)) {
                // 현재 사용자가 북마크 한 경우 체크
                isBookmarkedMap.put(voteId, true);
            }
        }

        // 최종 결과 맵에 통계 정보 저장
        result.put("optionVoteCountMap", optionVoteCountMap);
        result.put("commentCountMap", commentCountMap);
        result.put("likeCountMap", likeCountMap);
        result.put("isLikedMap", isLikedMap);
        result.put("isBookmarkedMap", isBookmarkedMap);

        return result;
    }

    // Vote 엔티티 페이지와 통계 데이터를 기반으로 LoadVoteDto 페이지를 생성하는 메서드
    public Page<LoadVoteDto> getLoadVoteDtos(Long userId, Page<Vote> votes, Map<String, Object> stats, Pageable pageable) {
        // 각 Vote 엔티티를 LoadVoteDto로 변환
        List<LoadVoteDto> dtos = votes.getContent().stream()
                .map(v -> LoadVoteDto.fromEntityWithAllMaps(
                        v,
                        userId,
                        voteSelectRepository,
                        (Map<Long, Integer>) stats.get("optionVoteCountMap"),
                        (Map<Long, Integer>) stats.get("commentCountMap"),
                        (Map<Long, Integer>) stats.get("likeCountMap"),
                        (Map<Long, Boolean>) stats.get("isLikedMap"),
                        (Map<Long, Boolean>) stats.get("isBookmarkedMap")
                ))
                .toList();

        // 새 PageImpl 객체로 결과 페이지 생성 및 반환
        return new PageImpl<>(dtos, pageable, votes.getTotalElements());
    }

}
