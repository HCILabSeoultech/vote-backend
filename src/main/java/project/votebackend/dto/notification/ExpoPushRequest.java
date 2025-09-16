package project.votebackend.dto.notification;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExpoPushRequest {
    private List<ExpoPushMessage> messages;
}
