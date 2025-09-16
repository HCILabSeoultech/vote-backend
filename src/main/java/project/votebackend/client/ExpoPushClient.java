package project.votebackend.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import project.votebackend.dto.notification.ExpoPushMessage;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExpoPushClient {

    private final WebClient webClient = WebClient.builder().build();

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    // 배열 보냄: List<ExpoPushMessage>
    public String send(List<ExpoPushMessage> messages) {
        return webClient.post()
                .uri(EXPO_PUSH_URL)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(messages) // <-- 포인트: wrapper 제거, 배열 그대로
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class).flatMap(err -> {
                            log.error("[EXPO-ERR] status={} body={}", resp.statusCode(), err);
                            return Mono.error(new RuntimeException("Expo Push HTTP " + resp.statusCode()));
                        })
                )
                .bodyToMono(String.class)           // 원문 먼저 확보
                .doOnNext(raw -> log.info("[EXPO-RAW] {}", raw))
                .block();
    }
}