package project.votebackend.dto.notification;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ExpoPushMessage {
    private String to;
    private String title;
    private String body;
    private Map<String, String> data;
    private String sound;
    private Integer ttl;
    private Integer expiration;
    private String priority;
    private Integer badge;
}
