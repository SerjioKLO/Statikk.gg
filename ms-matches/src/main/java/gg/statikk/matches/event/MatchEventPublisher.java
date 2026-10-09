package gg.statikk.matches.event;

import gg.statikk.matches.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MatchEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MatchEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public MatchEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishMatchCreated(MatchCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, "match.created", event);
        log.info("Evento publicado: match.created -> matchId={}, playerProfileId={}",
                event.matchId(), event.playerProfileId());
    }

    public void publishMatchDeleted(MatchDeletedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, "match.deleted", event);
        log.info("Evento publicado: match.deleted -> matchId={}, playerProfileId={}",
                event.matchId(), event.playerProfileId());
    }
}
