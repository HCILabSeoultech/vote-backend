package project.votebackend.dto.article;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ClusterDetailDto {
    private Long clusterId;
    private String imageUrl;
    private String title;
    private LocalDateTime createdAt;

    private String subtitle1;
    private String subtitle2;
    private String subtitle3;
    private String subtitle4;

    private String content1;
    private String content2;
    private String content3;
    private String content4;

    // 출처 기사
    private List<ArticleItemDto> articles;
}
