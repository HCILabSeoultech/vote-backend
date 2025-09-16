package project.votebackend.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import project.votebackend.dto.notification.ExpoPushRequest;
import project.votebackend.dto.notification.ExpoPushResponse;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExpoPushClient {

    private final WebClient webClient = WebClient.builder().build();

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    public ExpoPushResponse send(ExpoPushRequest request) {
        return webClient.post()
                .uri(EXPO_PUSH_URL)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ExpoPushResponse.class)
                .doOnNext(resp -> log.info("Expo Push 전송 성공: {}", resp))
                .onErrorResume(e -> {
                    log.error("Expo Push 전송 실패: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("Expo Push 전송 실패"));
                })
                .block();
    }
}
