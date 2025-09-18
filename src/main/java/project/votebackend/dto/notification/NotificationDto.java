package project.votebackend.dto.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import project.votebackend.domain.notification.Notification;
import project.votebackend.type.NotificationType;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class NotificationDto {
    private final Long id;
    private final NotificationType type;
    private final String title;
    private final String body;
    private final boolean isRead;
    private final LocalDateTime createdAt;
    private final Long voteId;
    private final Long commentId;
    private final Long followerId;

    public static NotificationDto from(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getVoteId(),
                notification.getCommentId(),
                notification.getFollowerId()
        );
    }
}
