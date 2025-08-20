package project.votebackend.dto.article;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ArticleItemDto {
    private String url;
    private String originalUrl;
    private String title;
    private String publisher;
    private LocalDateTime publishedAt;
}
