package project.votebackend.dto.vote;

import lombok.*;
import project.votebackend.domain.vote.Vote;
import project.votebackend.domain.vote.VoteImage;
import project.votebackend.domain.vote.VoteOption;
import project.votebackend.type.ReactionType;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteSummaryDto {
    private Long voteId;
    private String title;
    private String thumbnailImageUrl;
    private int totalVotes;              // 누적 투표 수
    private LocalDateTime finishTime;
    private int version;

    // 투표에 등록된 첫 이미지 썸네일 반환
    private static String extractThumbnail(Vote vote) {
        Optional<String> imageUrl = vote.getImages().stream()
                .findFirst()
                .map(VoteImage::getImageUrl); // Optional<String>

        if (imageUrl.isPresent()) {
            return imageUrl.get();
        }

        // 이미지가 없다면 옵션 이미지 중 첫 번째
        return vote.getOptions().stream()
                .map(VoteOption::getOptionImage)
                .filter(img -> img != null && !img.isEmpty())
                .findFirst()
                .orElse(null); // 또는 기본 이미지 URL
    }

    public static VoteSummaryDto from(Vote vote) {
        return VoteSummaryDto.builder()
                .voteId(vote.getVoteId())
                .title(vote.getTitle())
                .finishTime(vote.getFinishTime())
                .thumbnailImageUrl(VoteSummaryDto.extractThumbnail(vote))
                .totalVotes(vote.getSelections().size())
                .version(vote.getVersion())
                .build();
    }
}
