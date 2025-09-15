package project.votebackend.dto.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmNotification {
    private String title;
    private String body;
    private String image;
}
