package project.votebackend.service.article;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import project.votebackend.application.article.ClusterIngestMapper;
import project.votebackend.domain.article.Article;
import project.votebackend.domain.article.Cluster;
import project.votebackend.dto.article.IngestArticleDto;
import project.votebackend.dto.article.IngestClusterNode;
import project.votebackend.dto.article.IngestPayload;
import project.votebackend.repository.article.ArticleRepository;
import project.votebackend.repository.article.ClusterRepository;
import project.votebackend.type.Category;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ClusterIngestService {

    private final RestTemplate restTemplate;
    private final ClusterRepository clusterRepository;
    private final ArticleRepository articleRepository;
    private final ClusterIngestMapper mapper;

    @Value("${fastapi.api-key}")
    private String apiKey;

    @Value("${fastapi.secret}")
    private String secret;

    public void ingestFromUrl(String sourceUrl, Category category) {
        // 외부 API 호출 → Json을 자가 객체로 역직렬화
        IngestPayload payload = fetchPayloadWithHeaders(sourceUrl);

        if (payload != null) {
            ingest(payload, category);
        }
    }

    private IngestPayload fetchPayloadWithHeaders(String url) {
        String method = "GET";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        URI uri = URI.create(url);
        String signature = makeSignature(method, uri, timestamp, secret);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        headers.set("X-API-KEY", apiKey);
        headers.set("X-TIMESTAMP", timestamp);
        headers.set("X-SIGNATURE", signature);

        RequestEntity<Void> req = RequestEntity
                .get(uri)
                .headers(headers)
                .build();

        try {
            ResponseEntity<IngestPayload> res = restTemplate.exchange(req, IngestPayload.class);
            return res.getBody();
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("FastAPI 호출 실패: " + e.getRawStatusCode()
                    + " " + e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            throw new IllegalStateException("FastAPI 네트워크 오류", e);
        }
    }

    private String makeSignature(String method, URI uri, String timestamp, String secret) {
        String pathQuery = uri.getRawPath() +
                (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
        String message = method + "|" + pathQuery + "|" + timestamp + "|";
        return hmacSha256Base64(secret, message);
    }


    private String hmacSha256Base64(String secret, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 생성 실패", e);
        }
    }

    // 역직렬화 한 데이터를 DB에 저장
    @Transactional
    public void ingest(IngestPayload payload, Category category) {
        payload.getClusters().forEach((key, node) -> upsertOneCluster(node, category));
    }

    private void upsertOneCluster(IngestClusterNode node, Category category) {
        Cluster cluster = clusterRepository.findByTitle(node.getTitle())
                .orElseGet(Cluster::new);

        mapper.fillClusterFromNode(cluster, node);
        cluster.setCategory(category);
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
