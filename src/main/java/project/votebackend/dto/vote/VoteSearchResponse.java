package project.votebackend.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VoteSearchResponse {
    private Long id;
    private String title;
    private long totalVotes;
    private long likeCount;
    private long commentCount;
}
