package gg.statikk.notification.service;

import gg.statikk.notification.domain.Notification;
import gg.statikk.notification.domain.StatsSnapshot;
import gg.statikk.notification.event.StatsCalculatedEvent;
import gg.statikk.notification.repository.NotificationRepository;
import gg.statikk.notification.repository.StatsSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class NotificationService {

    public static final String TYPE_STATS_UPDATED = "STATS_UPDATED";

    private final NotificationRepository notificationRepository;
    private final StatsSnapshotRepository snapshotRepository;

    public NotificationService(NotificationRepository notificationRepository, StatsSnapshotRepository snapshotRepository) {
        this.notificationRepository = notificationRepository;
        this.snapshotRepository = snapshotRepository;
    }

    /**
     * Guarda la notificación (si corresponde) y actualiza la "foto" de métricas en UNA transacción.
     * Es naturalmente idempotente: si RabbitMQ entrega dos veces el mismo evento, la segunda vez las
     * métricas ya coinciden con la foto, no hay nada nuevo que decir y no se crea otra notificación.
     *
     * @return la notificación ya persistida (commit hecho al retornar), o vacío si nada cambió
     */
    @Transactional
    public Optional<Notification> recordStatsUpdate(Long userId, StatsCalculatedEvent event) {
        StatsSnapshot snapshot = snapshotRepository.findById(event.playerProfileId()).orElse(null);
        StatsView previous = snapshot == null ? null : snapshot.toView();
        StatsView current = new StatsView(event.totalMatches(), event.winRate(), event.kda());

        if (snapshot == null) {
            snapshot = new StatsSnapshot(event.playerProfileId());
        }
        snapshot.update(current);
        snapshotRepository.save(snapshot);

        return StatsMessageBuilder.build(previous, current)
                .map(content -> notificationRepository.save(
                        new Notification(userId, TYPE_STATS_UPDATED, content.title(), content.message())));
    }
}
