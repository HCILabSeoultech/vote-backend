package project.votebackend.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.votebackend.client.ExpoPushClient;
import project.votebackend.dto.notification.ExpoPushMessage;
import project.votebackend.dto.notification.ExpoPushRequest;
import project.votebackend.dto.notification.ExpoPushResponse;

import java.util.Arrays;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpoPushService {

    private final ExpoPushClient expoPushClient;

    public void sendBackgroundAlert(String expoPushToken, String title, String body, Map<String, String> data) {
        String maskedToken = expoPushToken != null && expoPushToken.length() > 20
                ? expoPushToken.substring(0, 20) + "..."
                : expoPushToken;

        log.info("[EXPO-SEND] 시작: token={}, title='{}', body='{}', data={}",
                maskedToken, title, body, data);

        // Expo 푸시 토큰 유효성 검증
        if (!isValidExpoPushToken(expoPushToken)) {
            log.warn("유효하지 않은 Expo 푸시 토큰: {}", maskedToken);
            return;
        }

        // Expo Push 메시지 생성
        ExpoPushMessage message = ExpoPushMessage.builder()
                .to(expoPushToken)
                .title(title)
                .body(body)
                .data(data)
                .sound("default")
                .priority("high")
                .ttl(3600) // 1시간 후 만료
                .build();

        ExpoPushRequest request = ExpoPushRequest.builder()
                .messages(Arrays.asList(message))
                .build();

        try {
            ExpoPushResponse response = expoPushClient.send(request);
            log.info("[EXPO-SEND] 성공: token={}, response={}", maskedToken, response);
        } catch (Exception ex) {
            log.error("[EXPO-SEND] 실패: token={}, msg={}", maskedToken, ex.getMessage(), ex);
            throw ex;
        }
    }

    private boolean isValidExpoPushToken(String token) {
        return token != null &&
                token.startsWith("ExponentPushToken[") &&
                token.endsWith("]");
    }
}
