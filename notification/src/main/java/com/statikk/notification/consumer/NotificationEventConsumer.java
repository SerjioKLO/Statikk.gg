package com.statikk.notification.consumer;

import com.statikk.notification.config.RabbitMQConfig;
import com.statikk.notification.event.ExcelMatchesImportedEvent;
import com.statikk.notification.event.MatchesUpdatedEvent;
import com.statikk.notification.event.StatsCalculatedEvent;
import com.statikk.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.EXCEL_IMPORTED_NOTIFICATION_QUEUE)
    public void handleExcelImported(ExcelMatchesImportedEvent event) {
        log.info("Received ExcelMatchesImportedEvent for userId {}", event.getUserId());
        String title = "Planilla procesada con éxito";
        String message = String.format("El archivo '%s' ha sido importado y validado correctamente.", event.getFilename());
        notificationService.createAndSendNotification(event.getUserId(), title, message, "FILE_PROCESSED");
    }

    @RabbitListener(queues = RabbitMQConfig.STATS_CALCULATED_NOTIFICATION_QUEUE)
    public void handleStatsCalculated(StatsCalculatedEvent event) {
        log.info("Received StatsCalculatedEvent for userId {}", event.getUserId());
        String title = "Estadísticas calculadas";
        String message = String.format("Se recalcularon las métricas del juego ID %d: Win Rate %.1f%%, Horas totales: %.1fh, KDA promedio: %.2f.",
                event.getGameId(), event.getWinRate(), event.getTotalHoursPlayed(), event.getAverageKda());
        notificationService.createAndSendNotification(event.getUserId(), title, message, "STATS_UPDATED");
    }

    @RabbitListener(queues = RabbitMQConfig.MATCHES_UPDATED_NOTIFICATION_QUEUE)
    public void handleMatchesUpdated(MatchesUpdatedEvent event) {
        log.info("Received MatchesUpdatedEvent for userId {}, eventType: {}", event.getUserId(), event.getEventType());
        String title = "Historial de partidas actualizado";
        String message = String.format("Se registraron cambios (%s) en el historial de partidas del juego ID %d.",
                event.getEventType(), event.getGameId());
        notificationService.createAndSendNotification(event.getUserId(), title, message, "MATCHES_MODIFIED");
    }
}
