package com.statikk.stats.consumer;

import com.statikk.stats.config.RabbitMQConfig;
import com.statikk.stats.event.GameDeletedEvent;
import com.statikk.stats.event.MatchesUpdatedEvent;
import com.statikk.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsEventConsumer {

    private final StatsService statsService;

    /**
     * Triggered whenever matches are imported, created, updated, or deleted
     */
    @RabbitListener(queues = RabbitMQConfig.MATCHES_UPDATED_STATS_QUEUE)
    public void handleMatchesUpdated(MatchesUpdatedEvent event) {
        log.info("Received MatchesUpdatedEvent for userId={} and gameId={}. Recalculating statistics.",
                event.getUserId(), event.getGameId());
        try {
            statsService.recalculateStatsForUserAndGame(event.getUserId(), event.getGameId());
            log.info("Successfully recalculated statistics for userId {} and gameId {}", event.getUserId(), event.getGameId());
        } catch (Exception e) {
            log.error("Error recalculating statistics on MatchesUpdatedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Triggered when a player removes a game (RF-07)
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_DELETED_STATS_QUEUE)
    public void handleGameDeleted(GameDeletedEvent event) {
        log.info("Received GameDeletedEvent for userId={} and gameId={}. Removing statistics.",
                event.getUserId(), event.getGameId());
        try {
            statsService.deleteStatsByUserAndGame(event.getUserId(), event.getGameId());
            log.info("Successfully removed statistics for deleted game ID {}", event.getGameId());
        } catch (Exception e) {
            log.error("Error removing statistics on GameDeletedEvent: {}", e.getMessage(), e);
        }
    }
}
