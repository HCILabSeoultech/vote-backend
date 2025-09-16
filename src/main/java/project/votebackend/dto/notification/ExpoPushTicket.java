package project.votebackend.dto.notification;

import lombok.Data;

@Data
public class ExpoPushTicket {
    private String status;
    private String id;
    private String message;
    private String details;
}
