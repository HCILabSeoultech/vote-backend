package project.votebackend.service.article;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import project.votebackend.application.article.ClusterIngestMapper;
import project.votebackend.domain.article.Article;
import project.votebackend.domain.article.Cluster;
import project.votebackend.dto.article.IngestArticleDto;
import project.votebackend.dto.article.IngestClusterNode;
import project.votebackend.dto.article.IngestPayload;
import project.votebackend.repository.article.ArticleRepository;
import project.votebackend.repository.article.ClusterRepository;

@Service
@RequiredArgsConstructor
public class ClusterIngestService {

    private final RestTemplate restTemplate;
    private final ClusterRepository clusterRepository;
    private final ArticleRepository articleRepository;
    private final ClusterIngestMapper mapper;

    public void ingestFromUrl(String sourceUrl) {
        // 외부 API 호출 → Json을 자가 객체로 역직렬화
        IngestPayload payload = restTemplate.getForObject(sourceUrl, IngestPayload.class);

        if (payload != null) {
            ingest(payload);
        }
    }

    // 역직렬화 한 데이터를 DB에 저장
    @Transactional
    public void ingest(IngestPayload payload) {
        payload.getClusters().forEach((key, node) -> upsertOneCluster(node));
    }

    private void upsertOneCluster(IngestClusterNode node) {
        Cluster cluster = clusterRepository.findByTitle(node.getTitle())
                .orElseGet(Cluster::new);

        mapper.fillClusterFromNode(cluster, node);
        Cluster saved = clusterRepository.save(cluster);

        if (node.getArticles() != null) {
            for (IngestArticleDto aDto : node.getArticles()) {
                boolean exists = articleRepository
                        .findByClusterIdAndUrl(saved.getId(), aDto.getUrl())
                        .isPresent();

                if (!exists) {
                    Article article = mapper.toArticleEntity(aDto, saved);
                    articleRepository.save(article);
                }
            }
        }
    }
}
