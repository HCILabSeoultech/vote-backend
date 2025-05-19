package project.votebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.votebackend.domain.Vote;
import project.votebackend.domain.VoteOption;
import project.votebackend.repository.VoteSelectRepository;
import project.votebackend.type.ReactionType;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoadVoteDto {
    private Long voteId;
    private String title;
    private String content;
    private String link;
    private String categoryName;
    private Long userId;
    private String username;
    private String name;
    private LocalDateTime createdAt;
    private List<VoteImageDto> images;
    private List<VoteOptionDto> voteOptions;
    private LocalDateTime finishTime;
    private int commentCount;
    private int likeCount;
    private String profileImage;

    @JsonProperty("isBookmarked")
    private boolean isBookmarked;
    @JsonProperty("isLiked")
    private boolean isLiked;

    private int totalVotes;
    private Long selectedOptionId;

    // Vote 엔티티를 DTO로 변환하는 기본 메서드 (DB 직접 조회 방식 사용)
    // 추후 수정하야할 부분
    public static LoadVoteDto fromEntity(Vote vote, Long currentUserId, VoteSelectRepository voteSelectRepository) {
        // 부모 댓글 개수 계산 (대댓글 제외)
        int commentCount = vote.getComments() != null
                ? (int) vote.getComments().stream()
                .filter(c -> c.getParent() == null)
                .count()
                : 0;

        // 좋아요 수 계산
        int likeCount = (int) vote.getReactions().stream()
                .filter(r -> r.getReaction() == ReactionType.LIKE)
                .distinct()
                .count();

        // 현재 사용자가 좋아요를 눌렀는지 여부
        boolean isLiked = vote.getReactions().stream()
                .anyMatch(r -> r.getUser().getUserId().equals(currentUserId)
                        && r.getReaction() == ReactionType.LIKE);

        // 현재 사용자가 북마크 했는지 여부
        boolean isBookmarked = vote.getReactions().stream()
                .anyMatch(r -> r.getUser().getUserId().equals(currentUserId)
                        && r.getReaction() == ReactionType.BOOKMARK);

        // 이미지 목록 변환
        List<VoteImageDto> images = vote.getImages().stream()
                .map(VoteImageDto::fromEntity)
                .collect(Collectors.toList());

        // 각 옵션별 투표 수 계산
        List<VoteOptionDto> voteOptions = vote.getOptions().stream()
                .sorted(Comparator.comparing(VoteOption::getOptionId)) // 옵션 순서 고정
                .map(option -> {
                    int voteCount = voteSelectRepository.countByOptionId(option.getOptionId()); // DB에서 옵션별 투표 수 조회
                    return VoteOptionDto.fromEntity(option, voteCount);
                })
                .collect(Collectors.toList());

        // 전체 투표 수 계산
        int totalVotes = voteOptions.stream()
                .mapToInt(VoteOptionDto::getVoteCount)
                .sum();

        // 사용자가 선택한 옵션 ID 조회
        Optional<Long> selectedOptionId = voteSelectRepository
                .findOptionIdByVoteIdAndUserId(vote.getVoteId(), currentUserId);

        // DTO 생성 및 반환
        return LoadVoteDto.builder()
                .voteId(vote.getVoteId())
                .title(vote.getTitle())
                .content(vote.getContent())
                .categoryName(vote.getCategory().getName())
                .userId(vote.getUser().getUserId())
                .username(vote.getUser().getUsername())
                .name(vote.getUser().getName())
                .createdAt(vote.getCreatedAt())
                .finishTime(vote.getFinishTime())
                .images(images)
                .voteOptions(voteOptions)
                .commentCount(commentCount)
                .likeCount(likeCount)
                .isLiked(isLiked)
                .profileImage(vote.getUser().getProfileImage())
                .isBookmarked(isBookmarked)
                .totalVotes(totalVotes)
                .selectedOptionId(selectedOptionId.orElse(null)) // 선택 안했으면 null
                .build();
    }

    // 쿼리 최적화를 위해 미리 계산된 통계 맵을 사용하는 변환 메서드
    public static LoadVoteDto fromEntityWithAllMaps(
            Vote vote,
            Long currentUserId,
            VoteSelectRepository voteSelectRepository,
            Map<Long, Integer> optionVoteCountMap,     // 옵션별 투표 수
            Map<Long, Integer> commentCountMap,        // 댓글 수
            Map<Long, Integer> likeCountMap,           // 좋아요 수
            Map<Long, Boolean> isLikedMap,             // 사용자가 좋아요 눌렀는지
            Map<Long, Boolean> isBookmarkedMap         // 사용자가 북마크 했는지
    ) {
        Long voteId = vote.getVoteId();

        // 통계 맵에서 값 가져오기 (없으면 기본값)
        int commentCount = commentCountMap.getOrDefault(voteId, 0);
        int likeCount = likeCountMap.getOrDefault(voteId, 0);
        boolean isLiked = isLikedMap.getOrDefault(voteId, false);
        boolean isBookmarked = isBookmarkedMap.getOrDefault(voteId, false);

        // 이미지 변환
        List<VoteImageDto> images = vote.getImages().stream()
                .map(VoteImageDto::fromEntity)
                .toList();

        // 옵션 및 옵션별 투표 수 변환
        List<VoteOptionDto> voteOptions = vote.getOptions().stream()
                .sorted(Comparator.comparing(VoteOption::getOptionId))
                .map(option -> {
                    int voteCount = optionVoteCountMap.getOrDefault(option.getOptionId(), 0);
                    return VoteOptionDto.fromEntity(option, voteCount);
                })
                .toList();

        // 전체 투표 수 계산
        int totalVotes = voteOptions.stream()
                .mapToInt(VoteOptionDto::getVoteCount)
                .sum();

        // 사용자가 선택한 옵션 ID 조회
        Optional<Long> selectedOptionId = voteSelectRepository
                .findOptionIdByVoteIdAndUserId(voteId, currentUserId);

        // DTO 생성 및 반환
        return LoadVoteDto.builder()
                .voteId(voteId)
                .title(vote.getTitle())
                .content(vote.getContent())
                .link(vote.getLink())
                .categoryName(vote.getCategory().getName())
                .userId(vote.getUser().getUserId())
                .username(vote.getUser().getUsername())
                .name(vote.getUser().getName())
                .createdAt(vote.getCreatedAt())
                .finishTime(vote.getFinishTime())
                .images(images)
                .voteOptions(voteOptions)
                .commentCount(commentCount)
                .likeCount(likeCount)
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .profileImage(vote.getUser().getProfileImage())
                .totalVotes(totalVotes)
                .selectedOptionId(selectedOptionId.orElse(null))
                .build();
    }

}
