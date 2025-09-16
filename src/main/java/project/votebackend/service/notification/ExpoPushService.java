package project.votebackend.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.votebackend.client.ExpoPushClient;
import project.votebackend.dto.notification.ExpoPushMessage;

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
                .ttl(3600)
                .build();

        try {
            String raw = expoPushClient.send(java.util.List.of(message));
            log.info("[EXPO-SEND] 성공: token={}, raw={}", maskedToken, raw);
            // 원한다면 raw 파싱해서 data[0].status == "error" 체크
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
