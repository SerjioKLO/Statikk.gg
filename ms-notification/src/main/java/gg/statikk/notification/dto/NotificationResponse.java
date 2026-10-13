package gg.statikk.notification.dto;

import gg.statikk.notification.domain.Notification;

import java.time.Instant;

/** Mismo formato por REST y por WebSocket. */
public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        Instant createdAt,
        boolean read
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.getCreatedAt(), n.getReadAt() != null);
    }
}
