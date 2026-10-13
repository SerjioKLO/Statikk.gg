package gg.statikk.notification.listener;

import feign.FeignException;
import gg.statikk.notification.client.PlayerClient;
import gg.statikk.notification.config.RabbitMQConfig;
import gg.statikk.notification.event.StatsCalculatedEvent;
import gg.statikk.notification.exception.PlayerServiceUnavailableException;
import gg.statikk.notification.service.NotificationDispatcher;
import gg.statikk.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StatsCalculatedListener {

    private static final Logger log = LoggerFactory.getLogger(StatsCalculatedListener.class);

    private final PlayerClient playerClient;
    private final NotificationService notificationService;
    private final NotificationDispatcher dispatcher;

    public StatsCalculatedListener(PlayerClient playerClient,
                                   NotificationService notificationService,
                                   NotificationDispatcher dispatcher) {
        this.playerClient = playerClient;
        this.notificationService = notificationService;
        this.dispatcher = dispatcher;
    }

    @RabbitListener(queues = RabbitMQConfig.STATS_EVENTS_QUEUE)
    public void onStatsCalculated(StatsCalculatedEvent event) {
        Long userId = resolveUserId(event.playerProfileId());
        if (userId == null) {
            return;
        }

        // recordStatsUpdate ya hizo commit cuando retorna: recién ahí se avisa (WebSocket/correo), así nunca
        // se notifica algo que después no existe en la base de datos.
        notificationService.recordStatsUpdate(userId, event).ifPresent(dispatcher::dispatch);
    }

    /** El evento trae el id del PERFIL; para notificar hace falta el id del USUARIO dueño, que vive en ms-player. */
    private Long resolveUserId(Long playerProfileId) {
        try {
            return playerClient.getProfile(playerProfileId).userId();
        } catch (FeignException.NotFound ex) {
            log.info("El perfil {} ya no existe; no hay a quién notificar", playerProfileId);
            return null; // sin reintento: el perfil no va a reaparecer
        } catch (RuntimeException ex) {
            throw new PlayerServiceUnavailableException(playerProfileId, ex); // con reintento (3 intentos)
        }
    }
}
