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
import project.votebackend.exception.FastApiException;
import project.votebackend.type.ErrorCode;

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
            return List.of();
        }

        List<AiBatchResult> results = aiVoteService.uploadBatch(requests);
        return results;
    }

    private List<CreateVoteRequest> fetchRequests(String url) {
        String method = "GET";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        URI uri = URI.create(url);

        String pathQuery = uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
        String message = method + "|" + pathQuery + "|" + timestamp + "|";
        String signature = hmacSha256Base64(secret, message);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("X-API-KEY", apiKey);
        headers.set("X-TIMESTAMP", timestamp);
        headers.set("X-SIGNATURE", signature);

        RequestEntity<Void> req = RequestEntity.get(uri).headers(headers).build();

        try {
            // 원문 먼저 확인 (구조/리다이렉트 문제 조기 포착)
            ResponseEntity<String> raw = restTemplate.exchange(req, String.class);

            if (!raw.getStatusCode().is2xxSuccessful()) {
                throw new FastApiException(ErrorCode.REQUEST_FAILED);
            }

            // 실제 파싱 (배열로 온다고 가정)
            ResponseEntity<List<CreateVoteRequest>> res = restTemplate.exchange(
                    req,
                    new ParameterizedTypeReference<List<CreateVoteRequest>>() {}
            );

            List<CreateVoteRequest> list = res.getBody();

            if (list != null && log.isDebugEnabled()) {
                for (int i = 0; i < Math.min(5, list.size()); i++) {
                    CreateVoteRequest r = list.get(i);
                    log.debug("[AI-VOTE:ITEM] idx={} title={} categoryId={} optionCount={} voteType={}",
                            i,
                            safe(r.getTitle()),
                            r.getCategoryId(),
                            r.getOptions() == null ? 0 : r.getOptions().size(),
                            r.getVoteType());
                }
                if (list.size() > 5) log.debug("[AI-VOTE:ITEM] ... ({} more)", list.size() - 5);
            }
            return list;

        } catch (RestClientResponseException e) {
            throw new FastApiException(ErrorCode.REQUEST_FAILED);
        } catch (org.springframework.web.client.RestClientException e) {
            throw new FastApiException(ErrorCode.RESPONSE_HANDLING_FAILED);
        }
    }

    private String hmacSha256Base64(String secret, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new FastApiException(ErrorCode.HMAC_GENERATE_FAILED);
        }
    }

    private static String safe(String s) {
        return s == null ? "(null)" : s.replaceAll("\\s+", " ").trim();
    }
}
