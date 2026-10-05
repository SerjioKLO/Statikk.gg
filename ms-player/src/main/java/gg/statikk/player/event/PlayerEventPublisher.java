package gg.statikk.player.event;

import gg.statikk.player.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PlayerEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PlayerEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public PlayerEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishGameDeleted(PlayerGameDeletedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, "player.game.deleted", event);
        log.info("Evento publicado: player.game.deleted -> playerId={}, gameId={}",
                event.playerProfileId(), event.gameId());
    }
}
