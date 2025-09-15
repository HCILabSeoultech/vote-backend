package project.votebackend.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import project.votebackend.dto.fcm.FcmSendRequest;
import project.votebackend.dto.fcm.FcmSendResponse;
import project.votebackend.exception.FcmException;
import project.votebackend.type.ErrorCode;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmClient {

    private final GoogleAccessTokenProvider tokenProvider;
    private final WebClient webClient = WebClient.builder().build();

    @Value("${fcm.project-id}")
    private String projectId;

    private String endpoint() {
        return "https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send";
    }

    public FcmSendResponse send(FcmSendRequest body) {
        String token = tokenProvider.getAccessToken();
        return webClient.post()
                .uri(endpoint())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(FcmSendResponse.class)
                .onErrorResume(e -> {
                    log.error("FCM 전송 실패: {}", e.getMessage(), e);
                    return Mono.error(new FcmException(ErrorCode.NOTIFICATION_FAILED));
                })
                .block();
    }
}
