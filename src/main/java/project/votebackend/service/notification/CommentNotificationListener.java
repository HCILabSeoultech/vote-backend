package project.votebackend.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.votebackend.dto.notification.CommentCreatedEvent;
import project.votebackend.repository.auth.DeviceTokenRepository;
import project.votebackend.repository.user.UserRepository;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentNotificationListener {

    private final ExpoPushService expoPushService; // FCM 대신 Expo 사용
    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentCreated(CommentCreatedEvent e) {
        log.info("[ALERT] CommentCreatedEvent 수신: voteId={}, commentId={}, commenterId={}, postAuthorId={}, parentAuthorId={}",
                e.getVoteId(), e.getCommentId(), e.getCommenterId(), e.getPostAuthorId(), e.getParentAuthorId());

        // 자기 자신 대상은 제외하고 중복 제거
        Set<Long> targets = new HashSet<>();
        if (!e.getCommenterId().equals(e.getPostAuthorId())) {
            targets.add(e.getPostAuthorId()); // 글 작성자
        }
        if (e.getParentAuthorId() != null && !e.getCommenterId().equals(e.getParentAuthorId())) {
            targets.add(e.getParentAuthorId()); // 부모 댓글 작성자(대댓글)
        }
        if (targets.isEmpty()) {
            log.info("[ALERT] 알림 대상 없음 (자기 자신만 댓글)");
            return;
        }

        String nickname = userRepository.findById(e.getCommenterId())
                .map(u -> Optional.ofNullable(u.getName()).orElse(u.getUsername()))
                .orElse("누군가");

        for (Long targetUserId : targets) {
            boolean isReplyTarget = (e.getParentAuthorId() != null && targetUserId.equals(e.getParentAuthorId()));

            String title = isReplyTarget ? "내 댓글에 답글이 달렸어요" : "내 글에 댓글이 달렸어요";
            String body  = isReplyTarget
                    ? nickname + " 님이 내 댓글에 답글을 남겼습니다."
                    : nickname + " 님이 내 글에 댓글을 남겼습니다.";

            Map<String, String> data = new HashMap<>();
            data.put("type", isReplyTarget ? "REPLY" : "COMMENT");
            data.put("voteId", String.valueOf(e.getVoteId()));
            data.put("commentId", String.valueOf(e.getCommentId()));
            data.put("deeplink", "votey://vote/" + e.getVoteId() + "?commentId=" + e.getCommentId());

            var tokens = deviceTokenRepository.findActiveTokensByUserId(targetUserId);

            log.info("[ALERT] targetUserId={}, nickname='{}', isReplyTarget={}, tokenCount={}, title='{}', body='{}'",
                    targetUserId, nickname, isReplyTarget, tokens.size(), title, body);

            for (String token : tokens) {
                try {
                    String masked = token.length() > 20 ? token.substring(0, 20) + "..." : token;
                    log.info("[ALERT->EXPO] userId={} token={} 전송 시작", targetUserId, masked);

                    expoPushService.sendBackgroundAlert(token, title, body, data);

                    log.info("[ALERT->EXPO] userId={} token={} 전송 성공", targetUserId, masked);
                } catch (Exception ex) {
                    log.warn("[ALERT->EXPO FAIL] userId={} token={} msg={}", targetUserId, token, ex.getMessage());
                }
            }
        }
    }
}
