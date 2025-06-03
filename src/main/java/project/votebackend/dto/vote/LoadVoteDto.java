package project.votebackend.dto.vote;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.votebackend.domain.vote.Vote;
import project.votebackend.domain.vote.VoteOption;
import project.votebackend.repository.vote.VoteSelectRepository;
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
