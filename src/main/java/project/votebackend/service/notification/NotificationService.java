package project.votebackend.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import project.votebackend.domain.notification.Notification;
import project.votebackend.dto.notification.NotificationDto;
import project.votebackend.exception.NotificationException;
import project.votebackend.repository.notification.NotificationRepository;
import project.votebackend.security.CustumUserDetails;
import project.votebackend.type.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> listForUser(Long userId, int page, int size) {
        Sort sort = Sort.by(Sort.Order.asc("isRead"), Sort.Order.desc("createdAt"));
        PageRequest pr = PageRequest.of(page, size, sort);
        return notificationRepository.findByTargetUserId(userId, pr)
                .map(NotificationDto::from);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getTargetUserId().equals(userId)) {
            throw new NotificationException(ErrorCode.NOT_MY_NOTIFICATION);
        }
        if (!notification.isRead()) {
            notification.setRead(true);
        }
    }
}
