package project.votebackend.application.article;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.votebackend.domain.article.Article;
import project.votebackend.domain.article.Cluster;
import project.votebackend.dto.article.IngestArticleDto;
import project.votebackend.dto.article.IngestClusterNode;

@Component
@RequiredArgsConstructor
public class ClusterIngestMapper {

    public void fillClusterFromNode(Cluster cluster, IngestClusterNode node) {
        cluster.setImageUrl(node.getImageUrl());
        cluster.setTitle(node.getTitle());
        cluster.setSourceArticleUrl(node.getSourceArticleUrl());
        cluster.setSourceOriginalArticleUrl(node.getSourceOriginalArticleUrl());

        cluster.setSubtitle1(node.getSubtitle1());
        cluster.setSubtitle2(node.getSubtitle2());
        cluster.setSubtitle3(node.getSubtitle3());
        cluster.setSubtitle4(node.getSubtitle4());

        cluster.setContent1(node.getContent1());
        cluster.setContent2(node.getContent2());
        cluster.setContent3(node.getContent3());
        cluster.setContent4(node.getContent4());
    }

    public Article toArticleEntity(IngestArticleDto dto, Cluster cluster) {
        Article a = new Article();
        a.setCluster(cluster);
        a.setUrl(dto.getUrl());
        a.setOriginalUrl(dto.getOriginalUrl());
        a.setTitle(dto.getTitle());
        a.setPublisher(dto.getPublisher());
        a.setPublishedAt(dto.getPublishedAt());
        return a;
    }
}
