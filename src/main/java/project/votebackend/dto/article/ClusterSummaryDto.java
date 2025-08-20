package project.votebackend.dto.article;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ClusterSummaryDto {
    private Long id;
    private String imageUrl;
    private String title;
    private LocalDateTime createdAt;
}
