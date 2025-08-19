package project.votebackend.dto.article;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class IngestArticleDto {

    private String url;

    @JsonProperty("original_url")
    private String originalUrl;

    private String title;
    private String publisher;

    @JsonProperty("published_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;
}
