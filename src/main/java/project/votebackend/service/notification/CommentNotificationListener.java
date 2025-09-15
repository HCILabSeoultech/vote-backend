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

    private final FcmMessageSendService fcmMessageSendService;
    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentCreated(CommentCreatedEvent e) {
        // 자기 자신 대상은 제외하고 중복 제거
        Set<Long> targets = new HashSet<>();
        if (!e.getCommenterId().equals(e.getPostAuthorId())) {
            targets.add(e.getPostAuthorId()); // 글 작성자
        }
        if (e.getParentAuthorId() != null && !e.getCommenterId().equals(e.getParentAuthorId())) {
            targets.add(e.getParentAuthorId()); // 부모 댓글 작성자(대댓글)
        }
        if (targets.isEmpty()) return;

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
            // 앱에서 처리할 딥링크 규약
            data.put("deeplink", "votey://vote/" + e.getVoteId() + "?commentId=" + e.getCommentId());

            var tokens = deviceTokenRepository.findActiveTokensByUserId(targetUserId);
            for (String token : tokens) {
                try {
                    fcmMessageSendService.sendBackgroundAlert(token, title, body, data);
                } catch (Exception ex) {
                    // FcmException 매핑해두셨다면 여기서 무효 토큰 정리도 가능
                    log.warn("FCM 전송 실패 userId={} token={} msg={}", targetUserId, token, ex.getMessage());
                }
            }
        }
    }
}
