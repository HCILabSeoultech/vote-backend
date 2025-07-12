package project.votebackend.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VoteSearchResponse {
    private Long id;
    private String title;
    private int totalVotes;
    private int likeCount;
    private int commentCount;
}
