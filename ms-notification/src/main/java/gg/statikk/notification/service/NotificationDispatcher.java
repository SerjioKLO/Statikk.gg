package gg.statikk.notification.service;

import gg.statikk.notification.config.WebSocketConfig;
import gg.statikk.notification.domain.Notification;
import gg.statikk.notification.domain.NotificationPreference;
import gg.statikk.notification.dto.NotificationResponse;
import gg.statikk.notification.repository.NotificationPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Entrega una notificación YA GUARDADA por los canales disponibles. Es "mejor esfuerzo": si el usuario no está
 * conectado o el SMTP falla, no se pierde nada (la notificación sigue en la base de datos y se consulta por
 * REST) y el fallo de un canal no impide el otro.
 */
@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender mailSender;
    private final NotificationPreferenceRepository preferenceRepository;
    private final boolean mailEnabled;
    private final String mailFrom;

    public NotificationDispatcher(SimpMessagingTemplate messagingTemplate,
                                  JavaMailSender mailSender,
                                  NotificationPreferenceRepository preferenceRepository,
                                  @Value("${notification.mail.enabled:false}") boolean mailEnabled,
                                  @Value("${notification.mail.from:notificaciones@statikk.gg}") String mailFrom) {
        this.messagingTemplate = messagingTemplate;
        this.mailSender = mailSender;
        this.preferenceRepository = preferenceRepository;
        this.mailEnabled = mailEnabled;
        this.mailFrom = mailFrom;
    }

    public void dispatch(Notification notification) {
        pushToWebSocket(notification);
        sendEmailIfEnabled(notification);
    }

    /** Si el usuario no tiene ninguna sesión abierta, el mensaje simplemente no llega a nadie (no es un error). */
    private void pushToWebSocket(Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    notification.getUserId().toString(),
                    WebSocketConfig.USER_QUEUE,
                    NotificationResponse.from(notification));
        } catch (RuntimeException ex) {
            log.warn("No se pudo enviar por WebSocket la notificación {}: {}", notification.getId(), ex.getMessage());
        }
    }

    private void sendEmailIfEnabled(Notification notification) {
        if (!mailEnabled) {
            return;
        }
        try {
            NotificationPreference preference = preferenceRepository.findById(notification.getUserId()).orElse(null);
            if (preference == null || !preference.isEmailEnabled() || preference.getEmail() == null) {
                return;
            }

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(mailFrom);
            mail.setTo(preference.getEmail());
            mail.setSubject("[Statikk.gg] " + notification.getTitle());
            mail.setText(notification.getMessage()
                    + "\n\nPuedes desactivar estos correos en tus preferencias de notificaciones.");
            mailSender.send(mail);
            log.info("Correo enviado para la notificación {} (usuario {})", notification.getId(), notification.getUserId());
        } catch (RuntimeException ex) {
            log.warn("No se pudo enviar el correo de la notificación {}: {}", notification.getId(), ex.getMessage());
        }
    }
}
