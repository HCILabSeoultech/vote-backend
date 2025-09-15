package project.votebackend.dto.fcm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FcmMessage {
    private String token;                    // 단일 디바이스
    private String topic;                    // 토픽 전송 시
    private FcmNotification notification;    // 알림 배너
    private Map<String, String> data;        // 커스텀 데이터
    private FcmAndroid android;              // 안드로이드 옵션
    private FcmApns apns;                    // iOS 옵션
}
