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
public class FcmAps {
    private Integer badge;

    @JsonProperty("content-available")
    private Integer contentAvailable;

    @JsonProperty("mutable-content")
    private Integer mutableContent;

    private String category;
    private FcmAlert alert;
}
