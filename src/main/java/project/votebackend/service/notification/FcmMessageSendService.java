package project.votebackend.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.votebackend.client.FcmClient;
import project.votebackend.dto.fcm.*;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmMessageSendService {

    private final FcmClient fcmClient;

    public void sendBackgroundAlert(String fcmToken, String title, String body,
                                    Map<String, String> dataOpt) {
        String maskedToken = fcmToken != null && fcmToken.length() > 10
                ? fcmToken.substring(0, 10) + "..."
                : fcmToken;

        log.info("[FCM-SEND] 시작: token={}, title='{}', body='{}', data={}",
                maskedToken, title, body, dataOpt);

        // Android 영역
        FcmAndroidNotification androidNotification = FcmAndroidNotification.builder()
                .channelId("default")      // 앱에서 생성한 채널 ID와 동일해야 함
                .clickAction("OPEN_APP")   // AndroidManifest 인텐트 필터와 연동
                .build();

        FcmAndroid android = FcmAndroid.builder()
                .priority("HIGH")
                .notification(androidNotification)
                .build();

        // iOS(APNs) 영역
        FcmAps aps = FcmAps.builder()
                .badge(1)
                .build();

        FcmApns apns = FcmApns.builder()
                .headers(Map.of("apns-priority", "10"))
                .payload(FcmApnsPayload.builder()
                        .aps(aps)
                        .build())
                .build();

        // 공통 notification (배너)
        FcmNotification notification = FcmNotification.builder()
                .title(title)
                .body(body)
                .build();

        // 최종 메시지
        FcmMessage msg = FcmMessage.builder()
                .token(fcmToken)
                .notification(notification)
                .data(dataOpt)
                .android(android)
                .apns(apns)
                .build();

        try {
            var resp = fcmClient.send(FcmSendRequest.builder().message(msg).build());
            log.info("[FCM-SEND] 성공: token={}, response={}", maskedToken, resp);
        } catch (Exception ex) {
            log.error("[FCM-SEND] 실패: token={}, msg={}", maskedToken, ex.getMessage(), ex);
            throw ex; // 필요하다면 여기서 예외를 다시 던지거나 무시할 수 있음
        }
    }
}
