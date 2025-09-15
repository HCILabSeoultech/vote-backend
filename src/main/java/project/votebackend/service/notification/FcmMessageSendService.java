package project.votebackend.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.votebackend.client.FcmClient;
import project.votebackend.dto.fcm.*;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FcmMessageSendService {

    private final FcmClient fcmClient;

    public void sendBackgroundAlert(String fcmToken, String title, String body,
                                    Map<String, String> dataOpt) {

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
                // .contentAvailable(1)   // 백그라운드 데이터 처리 시 활성화
                // .mutableContent(1)     // Notification Service Extension 사용 시
                // .category("OPEN_APP")  // iOS에서 카테고리 분기 시
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

        fcmClient.send(FcmSendRequest.builder().message(msg).build());
    }
}
