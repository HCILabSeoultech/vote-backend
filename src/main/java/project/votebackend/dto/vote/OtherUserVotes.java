package project.votebackend.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import project.votebackend.domain.vote.Vote;
import project.votebackend.domain.vote.VoteImage;
import project.votebackend.domain.vote.VoteOption;

import java.util.List;
import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtherUserVotes {
    private Long voteId;
    private String title;
    private String thumbnailUrl;

    public static Page<OtherUserVotes> otherUserVotes(Page<Vote> votes, Pageable pageable) {
        List<OtherUserVotes> content = votes.getContent().stream()
                .map(vote -> OtherUserVotes.builder()
                        .voteId(vote.getVoteId())
                        .title(vote.getTitle())
                        .thumbnailUrl(extractThumbnail(vote))
                        .build())
                .toList();

        return new PageImpl<>(content, pageable, votes.getTotalElements());
    }

    private static String extractThumbnail(Vote vote) {
        Optional<String> imageUrl = vote.getImages().stream()
                .findFirst()
                .map(VoteImage::getImageUrl);

        if (imageUrl.isPresent()) {
            return imageUrl.get();
        }

        return vote.getOptions().stream()
                .map(VoteOption::getOptionImage)
                .filter(img -> img != null && !img.isEmpty())
                .findFirst()
                .orElse(null);
    }
}
