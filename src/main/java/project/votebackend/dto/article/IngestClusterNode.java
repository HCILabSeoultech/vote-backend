package project.votebackend.dto.article;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class IngestClusterNode {

    @JsonProperty("cluster_id")
    private String clusterId;

    private List<IngestArticleDto> articles;

    @JsonProperty("source_article_url")
    private String sourceArticleUrl;

    @JsonProperty("source_original_article_url")
    private String sourceOriginalArticleUrl;

    @JsonProperty("image_url")
    private String imageUrl;

    private String title;

    private String subtitle1;
    private String subtitle2;
    private String subtitle3;
    private String subtitle4;

    private String content1;
    private String content2;
    private String content3;
    private String content4;
}
