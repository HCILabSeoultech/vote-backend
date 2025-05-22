package project.votebackend.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateVoteResponse {
    private String status;
    private Long postId;
}
