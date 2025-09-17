package project.votebackend.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FollowCreatedEvent {
    private final Long followerId;
    private final Long followingId;
}
