package project.votebackend.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.votebackend.client.FcmClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FcmMessageSendService {

    private final FcmClient fcmClient;

    public void sendBackgroundAlert(String fcmToken, String title, String body,
                                    Map<String, String> dataOpt) {
        FcmMessage msg = FcmMessage.builder()
                .token(fcmToken)
                .notification(FcmMessage.Notification.builder()
                        .title(title).body(body).build())
                .data(dataOpt)
                .android(FcmMessage.Android.builder()
                        .priority("HIGH")
                        .notification(FcmMessage.Android.AndroidNotification.builder()
                                .channel_id("default")
                                .click_action("OPEN_APP")
                                .build())
                        .build())
                .apns(FcmMessage.Apns.builder()
                        .headers(Map.of("apns-priority", "10"))
                        .payload(FcmMessage.Apns.ApnsPayload.builder()
                                .aps(FcmMessage.Apns.ApnsPayload.Aps.builder()
                                        .badge(1)
                                        .build())
                                .build())
                        .build())
                .build();

        fcmClient.send(FcmSendRequest.builder().message(msg).build());
    }
}
