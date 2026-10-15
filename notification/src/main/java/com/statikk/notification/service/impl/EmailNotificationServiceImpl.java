package com.statikk.notification.service.impl;

import com.statikk.notification.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@statikk.gg}")
    private String fromEmail;

    @Override
    public void sendNotificationEmail(String recipientEmail, String subject, String content) {
        log.info("Sending notification email to: {} with subject: '{}'", recipientEmail, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("Email sent successfully to {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send email notification to {}: {}", recipientEmail, e.getMessage());
        }
    }
}
