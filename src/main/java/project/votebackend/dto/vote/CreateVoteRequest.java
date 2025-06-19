package project.votebackend.dto.vote;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateVoteRequest {
    private Long userId;
    private Long categoryId;
    private String title;
    private String content;
    private String link;
    private String voteType;
    private LocalDateTime finishTime;
    private List<VoteOptionDto> options;
    private List<String> imageUrls;
}
