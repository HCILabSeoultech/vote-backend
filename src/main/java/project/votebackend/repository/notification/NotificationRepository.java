package project.votebackend.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import project.votebackend.domain.notification.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
