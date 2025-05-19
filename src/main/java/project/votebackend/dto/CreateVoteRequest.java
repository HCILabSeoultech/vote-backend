package project.votebackend.dto;

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
    private LocalDateTime finishTime;
    private List<VoteOptionDto> options;
    private List<String> imageUrls;
}
