package gg.statikk.matches.service;

import gg.statikk.matches.domain.Match;
import gg.statikk.matches.domain.MatchResult;
import gg.statikk.matches.domain.ProcessedImport;
import gg.statikk.matches.event.ExcelMatchesImportedEvent;
import gg.statikk.matches.repository.MatchRepository;
import gg.statikk.matches.repository.ProcessedImportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;

@Service
public class MatchImportService {

    private static final Logger log = LoggerFactory.getLogger(MatchImportService.class);

    private final MatchRepository matchRepository;
    private final ProcessedImportRepository processedImportRepository;

    public MatchImportService(MatchRepository matchRepository, ProcessedImportRepository processedImportRepository) {
        this.matchRepository = matchRepository;
        this.processedImportRepository = processedImportRepository;
    }

    /**
     * Inserta todas las partidas del evento en UNA transacción (todo o nada) y registra el importId.
     *
     * @return cuántas partidas se insertaron, o vacío si esta importación ya se había procesado.
     */
    @Transactional
    public OptionalInt importFromExcel(ExcelMatchesImportedEvent event) {
        if (processedImportRepository.existsById(event.importId())) {
            log.warn("Importación {} ya procesada; se ignora la entrega duplicada", event.importId());
            return OptionalInt.empty();
        }

        List<Match> matches = event.matches().stream()
                .map(row -> new Match(
                        event.playerProfileId(), row.gameName(),
                        MatchResult.valueOf(row.result().toUpperCase(Locale.ROOT)),
                        row.durationMinutes(), row.kills(), row.deaths(), row.assists(), row.playedAt()))
                .toList();

        matchRepository.saveAll(matches);
        processedImportRepository.save(new ProcessedImport(event.importId(), event.playerProfileId(), matches.size()));

        return OptionalInt.of(matches.size());
    }
}
