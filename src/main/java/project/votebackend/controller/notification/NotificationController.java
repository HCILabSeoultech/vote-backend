package project.votebackend.controller.notification;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.votebackend.dto.notification.NotificationDto;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.service.notification.NotificationService;

import java.util.Map;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    @Operation(summary = "알림 목록 조회 API", description = "내 알림들을 조회합니다. (내용, 시간, 읽음 여부 포함)")
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal CustumUserDetails userDetails,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {

        Page<NotificationDto> notifications = notificationService.listForUser(userDetails.getId(), page, size);

        return ResponseEntity.ok(notifications);
    }

    // 개별 읽음 처리
    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리 API", description = "특정 알림을 읽음 처리합니다.")
    public ResponseEntity<?> markRead(@PathVariable Long notificationId,
                                      @AuthenticationPrincipal CustumUserDetails userDetails) {
        notificationService.markRead(userDetails.getId(), notificationId);
        return ResponseEntity.ok(Map.of("message", "알림 읽음 처리 완료"));
    }
}
