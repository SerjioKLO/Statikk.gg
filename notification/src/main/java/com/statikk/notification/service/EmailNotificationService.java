package com.statikk.notification.service;

public interface EmailNotificationService {
    void sendNotificationEmail(String recipientEmail, String subject, String content);
}
