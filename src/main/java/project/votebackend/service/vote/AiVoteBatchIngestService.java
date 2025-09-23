package project.votebackend.service.vote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import project.votebackend.dto.vote.AiBatchResult;
import project.votebackend.dto.vote.CreateVoteRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiVoteBatchIngestService {

    private final RestTemplate restTemplate;
    private final AiVoteService aiVoteService;

    @Value("${fastapi.api-key}")
    private String apiKey;

    @Value("${fastapi.secret}")
    private String secret;

    public List<AiBatchResult> ingestVotesFromUrl(String sourceUrl) {
        List<CreateVoteRequest> requests = fetchRequests(sourceUrl);

        if (requests == null || requests.isEmpty()) {
            log.info("[AI-VOTE] empty payload from {}", sourceUrl);
            return List.of();
        }

        log.info("[AI-VOTE] uploading batch size={} url={}", requests.size(), sourceUrl);
        return aiVoteService.uploadBatch(requests);
    }

    private List<CreateVoteRequest> fetchRequests(String url) {
        String method = "GET";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        URI uri = URI.create(url);
        String signature = makeSignature(method, uri, timestamp, secret);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("X-API-KEY", apiKey);
        headers.set("X-TIMESTAMP", timestamp);
        headers.set("X-SIGNATURE", signature);

        RequestEntity<Void> req = RequestEntity.get(uri).headers(headers).build();

        try {
            ResponseEntity<List<CreateVoteRequest>> res = restTemplate.exchange(
                    req,
                    new ParameterizedTypeReference<List<CreateVoteRequest>>() {}
            );
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
}
