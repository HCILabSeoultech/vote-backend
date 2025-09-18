package project.votebackend.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.votebackend.domain.notification.Notification;
import project.votebackend.dto.notification.FollowCreatedEvent;
import project.votebackend.repository.auth.DeviceTokenRepository;
import project.votebackend.repository.user.UserRepository;
import project.votebackend.type.NotificationType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class FollowNotificationListener {

    private final ExpoPushService expoPushService;
    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFollowCreated(FollowCreatedEvent e) {
        log.info("[ALERT] FollowCreatedEvent: followerId={}, followingId={}", e.getFollowerId(), e.getFollowingId());

        if (e.getFollowerId().equals(e.getFollowingId())) return;

        String followerName = userRepository.findById(e.getFollowerId())
                .map(u -> u.getName() != null ? u.getName() : u.getUsername())
                .orElse("누군가");

        String title = "새 팔로워가 생겼어요";
        String body  = followerName + "님이 나를 팔로우했습니다.";

        Map<String, String> data = new HashMap<>();
        data.put("type", "FOLLOW");
        data.put("followerId", String.valueOf(e.getFollowerId()));
        data.put("deeplink", "votey://user/" + e.getFollowerId()); // RN에서 이 스킴 처리

        List<String> tokens = deviceTokenRepository.findActiveTokensByUserId(e.getFollowingId());
        log.info("[ALERT] follow targetUserId={}, tokenCount={}", e.getFollowingId(), tokens.size());

        notificationService.save(
                Notification.builder()
                        .targetUserId(e.getFollowingId())
                        .type(NotificationType.FOLLOW)
                        .title(title)
                        .body(body)
                        .followerId(e.getFollowerId())
                        .build()
        );

        for (String token : tokens) {
            try {
                expoPushService.sendBackgroundAlert(token, title, body, data);
            } catch (Exception ex) {
                log.warn("[ALERT->EXPO FAIL] userId={} msg={}", e.getFollowingId(), ex.getMessage());
            }
        }
    }
}
