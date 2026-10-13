package gg.statikk.notification.service;

import gg.statikk.notification.domain.Notification;
import gg.statikk.notification.dto.MarkAllReadResponse;
import gg.statikk.notification.dto.NotificationResponse;
import gg.statikk.notification.dto.UnreadCountResponse;
import gg.statikk.notification.exception.NotificationNotFoundException;
import gg.statikk.notification.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationQueryService {

    private static final int MAX_LIMIT = 100;

    private final NotificationRepository notificationRepository;

    public NotificationQueryService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(Long userId, boolean unreadOnly, int limit) {
        Pageable page = PageRequest.of(0, Math.min(Math.max(limit, 1), MAX_LIMIT));
        List<Notification> found = unreadOnly
                ? notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId, page)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, page);
        return found.stream().map(NotificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(Long userId) {
        return new UnreadCountResponse(notificationRepository.countByUserIdAndReadAtIsNull(userId));
    }

    @Transactional
    public NotificationResponse markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        notification.markRead(Instant.now());
        return NotificationResponse.from(notification);
    }

    @Transactional
    public MarkAllReadResponse markAllRead(Long userId) {
        return new MarkAllReadResponse(notificationRepository.markAllRead(userId, Instant.now()));
    }
}
