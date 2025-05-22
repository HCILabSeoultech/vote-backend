package project.votebackend.dto.voteSelect;

import lombok.Data;

@Data
public class VoteSelectRequest {
    private Long userId;
    private Long voteId;
    private Long optionId;
}
