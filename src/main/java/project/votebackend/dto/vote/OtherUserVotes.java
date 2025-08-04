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

    public static Page<VoteSummaryDto> otherUserVotes(Page<Vote> votes, Pageable pageable) {
        List<VoteSummaryDto> content = votes.getContent().stream()
                .map(VoteSummaryDto::from)
                .toList();

        return new PageImpl<>(content, pageable, votes.getTotalElements());
    }
}
