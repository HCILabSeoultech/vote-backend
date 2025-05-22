package project.votebackend.dto.voteSelect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class VoteSelectResponse {
    private Long voteId;
    private Long optionId;
    private String optionContent;
    private Long userId;
}
