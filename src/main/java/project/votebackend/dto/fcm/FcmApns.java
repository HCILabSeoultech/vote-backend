package project.votebackend.dto.fcm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmApns {
    private Map<String, String> headers;
    private FcmApnsPayload payload;
    @JsonProperty("fcm_options")
    private FcmOptions fcmOptions;
}
