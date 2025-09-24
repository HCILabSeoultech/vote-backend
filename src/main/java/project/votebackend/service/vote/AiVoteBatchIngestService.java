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
        long t0 = System.currentTimeMillis();
        log.info("[AI-VOTE] ingest start url={}", sourceUrl);

        List<CreateVoteRequest> requests = fetchRequests(sourceUrl);

        if (requests == null || requests.isEmpty()) {
            log.info("[AI-VOTE] empty payload from {} (elapsed={}ms)", sourceUrl, System.currentTimeMillis() - t0);
            return List.of();
        }

        log.info("[AI-VOTE] uploading batch size={} url={} (elapsed={}ms)",
                requests.size(), sourceUrl, System.currentTimeMillis() - t0);

        List<AiBatchResult> results = aiVoteService.uploadBatch(requests);

        long t1 = System.currentTimeMillis();
        long ok = results.stream().filter(AiBatchResult::isSuccess).count();
        long fail = results.size() - ok;
        log.info("[AI-VOTE] ingest done url={} count={} ok={} fail={} (elapsed={}ms)",
                sourceUrl, results.size(), ok, fail, (t1 - t0));

        return results;
    }

    private List<CreateVoteRequest> fetchRequests(String url) {
        String method = "GET";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        URI uri = URI.create(url);

        String pathQuery = uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
        String message = method + "|" + pathQuery + "|" + timestamp + "|";
        String signature = hmacSha256Base64(secret, message);

        // 민감정보는 마스킹
        log.info("[AI-VOTE:REQ] method={} url={} pathQuery={} ts={} msg={}",
                method, url, pathQuery, timestamp, message);
        log.debug("[AI-VOTE:SIG] signature(base64)={} (masked={})",
                signature, mask(signature));

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("X-API-KEY", apiKey);
        headers.set("X-TIMESTAMP", timestamp);
        headers.set("X-SIGNATURE", signature);

        // 로그: 마스킹해서 찍기
        log.info("[AI-VOTE:REQ] method={} url={} pathQuery={} ts={} msg={} X-API-KEY.len={}",
                method, url, pathQuery, timestamp, message,
                apiKey == null ? -1 : apiKey.length());
        log.debug("[AI-VOTE:SIG] signature(base64)={}  apiKey(masked)={} ",
                signature, mask(apiKey));
        RequestEntity<Void> req = RequestEntity.get(uri).headers(headers).build();

        try {
            // 원문 먼저 확인 (구조/리다이렉트 문제 조기 포착)
            ResponseEntity<String> raw = restTemplate.exchange(req, String.class);
            String bodyPreview = preview(raw.getBody(), 800);
            log.info("[AI-VOTE:RAW] status={} len={} bodyPreview={}",
                    raw.getStatusCodeValue(),
                    raw.getBody() == null ? 0 : raw.getBody().length(),
                    bodyPreview);

            if (!raw.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("FastAPI 호출 실패: " + raw.getStatusCodeValue() + " " + bodyPreview);
            }

            // 실제 파싱 (배열로 온다고 가정)
            ResponseEntity<List<CreateVoteRequest>> res = restTemplate.exchange(
                    req,
                    new ParameterizedTypeReference<List<CreateVoteRequest>>() {}
            );

            List<CreateVoteRequest> list = res.getBody();
            log.info("[AI-VOTE:PARSE] parsedCount={} status={}",
                    list == null ? 0 : list.size(),
                    res.getStatusCodeValue());

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
            log.error("[AI-VOTE:HTTP-ERR] status={} body={}", e.getRawStatusCode(), preview(e.getResponseBodyAsString(), 800), e);
            throw new IllegalStateException("FastAPI 호출 실패: " + e.getRawStatusCode()
                    + " " + preview(e.getResponseBodyAsString(), 400), e);
        } catch (org.springframework.web.client.RestClientException e) {
            // 매핑/네트워크 등 포괄
            log.error("[AI-VOTE:CLIENT-ERR] {}", e.getMessage(), e);
            throw new IllegalStateException("FastAPI 응답 처리 실패: " + e.getMessage(), e);
        }
    }

    private String makeSignature(String method, URI uri, String timestamp, String secret) {
        String pathQuery = uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
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
            log.error("[AI-VOTE:HMAC-ERR] {}", e.getMessage(), e);
            throw new IllegalStateException("HMAC 생성 실패", e);
        }
    }

    // ===== 유틸 =====

    private static String mask(String s) {
        if (s == null) return null;
        int n = s.length();
        if (n <= 4) return "****";
        return s.substring(0, 2) + "****" + s.substring(n - 2);
    }

    private static String preview(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...(+" + (s.length() - max) + " chars)";
    }

    private static String safe(String s) {
        return s == null ? "(null)" : s.replaceAll("\\s+", " ").trim();
    }
}
