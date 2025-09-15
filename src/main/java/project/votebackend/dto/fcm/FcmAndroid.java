package project.votebackend.dto.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmAndroid {
    private FcmAndroidNotification notification;
    private String priority;
}
