package gg.statikk.stats.event;

import gg.statikk.stats.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class StatsEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(StatsEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public StatsEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishStatsCalculated(StatsCalculatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, "stats.calculated", event);
        log.info("Evento publicado: stats.calculated -> playerProfileId={}, partidas={}",
                event.playerProfileId(), event.totalMatches());
    }
}
