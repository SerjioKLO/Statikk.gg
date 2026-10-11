package gg.statikk.matches.listener;

import gg.statikk.matches.config.RabbitMQConfig;
import gg.statikk.matches.event.ExcelMatchesImportedEvent;
import gg.statikk.matches.event.MatchEventPublisher;
import gg.statikk.matches.event.MatchesBulkImportedEvent;
import gg.statikk.matches.service.MatchImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.OptionalInt;

@Component
public class ExcelImportListener {

    private static final Logger log = LoggerFactory.getLogger(ExcelImportListener.class);

    private final MatchImportService matchImportService;
    private final MatchEventPublisher eventPublisher;

    public ExcelImportListener(MatchImportService matchImportService, MatchEventPublisher eventPublisher) {
        this.matchImportService = matchImportService;
        this.eventPublisher = eventPublisher;
    }

    @RabbitListener(queues = RabbitMQConfig.EXCEL_IMPORT_QUEUE)
    public void onExcelMatchesImported(ExcelMatchesImportedEvent event) {
        log.info("Recibida importación {}: {} partidas para el perfil {}",
                event.importId(), event.matches().size(), event.playerProfileId());

        // La transacción de importFromExcel ya hizo commit cuando esta llamada retorna;
        // recién ahí avisamos a los demás servicios.
        OptionalInt imported = matchImportService.importFromExcel(event);

        imported.ifPresent(count -> eventPublisher.publishMatchesBulkImported(new MatchesBulkImportedEvent(
                event.importId(), event.playerProfileId(), count, Instant.now())));
    }
}
