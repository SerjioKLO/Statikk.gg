package gg.statikk.stats.listener;

import gg.statikk.stats.config.RabbitMQConfig;
import gg.statikk.stats.event.MatchesChangedEvent;
import gg.statikk.stats.service.PendingRecalcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Lado de COMANDOS (CQRS): ante cualquier cambio en las partidas de un jugador solo lo marca como
 * pendiente (una escritura barata). El cálculo pesado lo hace el job batch, en lote y fuera de la
 * ruta del evento, así una importación de miles de partidas no se traduce en miles de recálculos.
 */
@Component
public class StatsEventListener {

    private static final Logger log = LoggerFactory.getLogger(StatsEventListener.class);

    private final PendingRecalcService pendingRecalcService;

    public StatsEventListener(PendingRecalcService pendingRecalcService) {
        this.pendingRecalcService = pendingRecalcService;
    }

    @RabbitListener(queues = RabbitMQConfig.MATCH_EVENTS_QUEUE)
    public void onMatchesChanged(MatchesChangedEvent event,
                                 @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        if (event.playerProfileId() == null) {
            log.warn("Evento {} sin playerProfileId; se ignora", routingKey);
            return;
        }
        pendingRecalcService.markDirty(event.playerProfileId());
        log.debug("{} -> jugador {} marcado para recálculo", routingKey, event.playerProfileId());
    }
}
