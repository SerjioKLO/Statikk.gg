package gg.statikk.matches.listener;

import gg.statikk.matches.config.RabbitMQConfig;
import gg.statikk.matches.event.MatchEventPublisher;
import gg.statikk.matches.event.MatchesBulkDeletedEvent;
import gg.statikk.matches.event.PlayerGameDeletedEvent;
import gg.statikk.matches.service.MatchCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Borrado en cascada: si el jugador quita un videojuego de su perfil, sus partidas de ese juego se eliminan. */
@Component
public class PlayerGameDeletedListener {

    private static final Logger log = LoggerFactory.getLogger(PlayerGameDeletedListener.class);

    private final MatchCleanupService cleanupService;
    private final MatchEventPublisher eventPublisher;

    public PlayerGameDeletedListener(MatchCleanupService cleanupService, MatchEventPublisher eventPublisher) {
        this.cleanupService = cleanupService;
        this.eventPublisher = eventPublisher;
    }

    @RabbitListener(queues = RabbitMQConfig.PLAYER_GAME_DELETED_QUEUE)
    public void onPlayerGameDeleted(PlayerGameDeletedEvent event) {
        log.info("player.game.deleted: borrando partidas de '{}' del perfil {}", event.gameName(), event.playerProfileId());

        // El borrado ya hizo commit cuando retorna. Se publica SIEMPRE, incluso con 0 borradas: si una entrega
        // anterior borró y murió antes de avisar, el reintento encuentra 0 y aun así debe avisar a ms-stats.
        int deleted = cleanupService.deleteMatchesOfGame(event.playerProfileId(), event.gameName());

        eventPublisher.publishMatchesBulkDeleted(new MatchesBulkDeletedEvent(
                event.playerProfileId(), event.gameName(), deleted, Instant.now()));
    }
}
