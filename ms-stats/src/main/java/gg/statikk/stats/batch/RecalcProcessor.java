package gg.statikk.stats.batch;

import gg.statikk.stats.client.MatchClientResponse;
import gg.statikk.stats.client.MatchesClient;
import gg.statikk.stats.service.StatsCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecalcProcessor implements ItemProcessor<PendingRecalc, PlayerRecalcResult> {

    private static final Logger log = LoggerFactory.getLogger(RecalcProcessor.class);

    private final MatchesClient matchesClient;
    private final StatsCalculator calculator;

    public RecalcProcessor(MatchesClient matchesClient, StatsCalculator calculator) {
        this.matchesClient = matchesClient;
        this.calculator = calculator;
    }

    @Override
    public PlayerRecalcResult process(PendingRecalc item) {
        try {
            List<MatchClientResponse> matches = matchesClient.listByPlayer(item.playerProfileId());
            List<MatchClientResponse> safe = matches == null ? List.of() : matches;
            return new PlayerRecalcResult(item.playerProfileId(), item.version(), calculator.aggregate(safe));
        } catch (RuntimeException ex) {
            // ms-matches caído o lento: devolver null "filtra" el ítem. El marcador sigue en la tabla
            // y el jugador se reintenta solo en la próxima corrida; el resto del lote no se ve afectado.
            log.warn("No se pudo recalcular al jugador {} (ms-matches no respondió): {}",
                    item.playerProfileId(), ex.getMessage());
            return null;
        }
    }
}
