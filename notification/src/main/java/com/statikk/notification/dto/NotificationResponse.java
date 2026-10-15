package com.statikk.notification.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private String message;
    private Long id;
    private long unreadCount;
}
