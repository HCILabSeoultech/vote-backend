package project.votebackend.dto.notification;

import lombok.Data;

import java.util.List;

@Data
public class ExpoPushResponse {
    private List<ExpoPushTicket> data;
}
