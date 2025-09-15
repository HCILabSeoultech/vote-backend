package project.votebackend.dto.fcm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmAndroidNotification {
    @JsonProperty("channel_id")
    private String channelId;
    private String image;
    @JsonProperty("click_action")
    private String clickAction;
}
