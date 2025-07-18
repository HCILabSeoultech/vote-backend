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

    //Entity -> Dto 변환 후 반환
    public static LoadVoteDto fromEntity(Vote vote, Long currentUserId, VoteSelectRepository voteSelectRepository) {
        int commentCount = vote.getComments() != null
                ? (int) vote.getComments().stream()
                .filter(c -> c.getParent() == null) // 부모 댓글만 필터링
                .count()
                : 0;
        int likeCount = (int) vote.getReactions().stream()
                .filter(r -> r.getReaction() == ReactionType.LIKE)
                .distinct()
                .count();

        boolean isLiked = vote.getReactions().stream()
                .anyMatch(r -> r.getUser().getUserId().equals(currentUserId)
                        && r.getReaction() == ReactionType.LIKE);

        boolean isBookmarked = vote.getReactions().stream()
                .anyMatch(r -> r.getUser().getUserId().equals(currentUserId)
                        && r.getReaction() == ReactionType.BOOKMARK);

        List<VoteImageDto> images = vote.getImages().stream()
                .map(VoteImageDto::fromEntity)
                .collect(Collectors.toList());

        // 각 옵션의 투표 수 계산
        List<VoteOptionDto> voteOptions = vote.getOptions().stream()
                .sorted(Comparator.comparing(VoteOption::getOptionId))
                .map(option -> {
                    int voteCount = voteSelectRepository.countByOptionId(option.getOptionId());
                    return VoteOptionDto.fromEntity(option, voteCount);
                })
                .collect(Collectors.toList());

        // 전체 투표 수 계산
        int totalVotes = voteOptions.stream()
                .mapToInt(VoteOptionDto::getVoteCount)
                .sum();

        // 사용자가 선택한 옵션 조회
        Optional<Long> selectedOptionId = voteSelectRepository
                .findOptionIdByVoteIdAndUserId(vote.getVoteId(), currentUserId);

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
                .selectedOptionId(selectedOptionId.orElse(null))
                .build();
    }

    public static LoadVoteDto fromEntityWithAllMaps(
            Vote vote,
            Long currentUserId,
            VoteSelectRepository voteSelectRepository,
            Map<Long, Integer> optionVoteCountMap,
            Map<Long, Integer> commentCountMap,
            Map<Long, Integer> likeCountMap,
            Map<Long, Boolean> isLikedMap,
            Map<Long, Boolean> isBookmarkedMap
    ) {
        Long voteId = vote.getVoteId();

        // 댓글 수
        int commentCount = commentCountMap.getOrDefault(voteId, 0);

        // 좋아요 수
        int likeCount = likeCountMap.getOrDefault(voteId, 0);

        // 내 반응
        boolean isLiked = isLikedMap.getOrDefault(voteId, false);
        boolean isBookmarked = isBookmarkedMap.getOrDefault(voteId, false);

        // 이미지
        List<VoteImageDto> images = vote.getImages().stream()
                .map(VoteImageDto::fromEntity)
                .toList();

        // 옵션 및 옵션별 투표 수
        List<VoteOptionDto> voteOptions = vote.getOptions().stream()
                .sorted(Comparator.comparing(VoteOption::getOptionId))
                .map(option -> {
                    int voteCount = optionVoteCountMap.getOrDefault(option.getOptionId(), 0);
                    return VoteOptionDto.fromEntity(option, voteCount);
                })
                .toList();

        // 전체 투표 수
        int totalVotes = voteOptions.stream()
                .mapToInt(VoteOptionDto::getVoteCount)
                .sum();

        // 사용자가 선택한 옵션
        Optional<Long> selectedOptionId = voteSelectRepository
                .findOptionIdByVoteIdAndUserId(voteId, currentUserId);

        return LoadVoteDto.builder()
                .voteId(voteId)
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
                .isBookmarked(isBookmarked)
                .profileImage(vote.getUser().getProfileImage())
                .totalVotes(totalVotes)
                .selectedOptionId(selectedOptionId.orElse(null))
                .build();
    }
}
