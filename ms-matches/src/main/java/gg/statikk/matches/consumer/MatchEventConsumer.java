package gg.statikk.matches.consumer;

import gg.statikk.matches.config.RabbitMQConfig;
import gg.statikk.matches.event.ExcelMatchesImportedEvent;
import gg.statikk.matches.event.GameDeletedEvent;
import gg.statikk.matches.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchEventConsumer {

    private final MatchService matchService;

    /**
     * Consumes parsed matches imported from Excel/CSV (ms-excel-parser)
     */
    @RabbitListener(queues = RabbitMQConfig.EXCEL_MATCHES_IMPORTED_QUEUE)
    public void handleExcelMatchesImported(ExcelMatchesImportedEvent event) {
        log.info("Received ExcelMatchesImportedEvent: userId={}, gameId={}, count={}",
                event.getUserId(), event.getGameId(), event.getMatches() != null ? event.getMatches().size() : 0);
        try {
            int saved = matchService.processImportedMatches(event);
            log.info("Successfully imported and saved {} matches", saved);
        } catch (Exception e) {
            log.error("Error processing ExcelMatchesImportedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Consumes GameDeletedEvent (RF-07) from ms-player and removes all associated matches
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_DELETED_MATCHES_QUEUE)
    public void handleGameDeleted(GameDeletedEvent event) {
        log.info("Received GameDeletedEvent for userId={} and gameId={}. Purging match history.",
                event.getUserId(), event.getGameId());
        try {
            matchService.deleteMatchesByUserAndGame(event.getUserId(), event.getGameId());
            log.info("Successfully purged matches for deleted game ID {}", event.getGameId());
        } catch (Exception e) {
            log.error("Error handling GameDeletedEvent: {}", e.getMessage(), e);
        }
    }
}
