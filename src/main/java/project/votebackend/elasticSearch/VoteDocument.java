package project.votebackend.elasticSearch;

import lombok.*;
import project.votebackend.domain.vote.Vote;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteDocument {
    private Long id;
    private String title;
    private String username;
    private String category;

    public static VoteDocument fromEntity(Vote vote) {
        return new VoteDocument(
                vote.getVoteId(),
                vote.getTitle(),
                vote.getUser().getName(),
                vote.getCategory().getName()
        );
    }
}