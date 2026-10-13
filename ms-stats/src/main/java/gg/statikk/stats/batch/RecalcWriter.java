package gg.statikk.stats.batch;

import gg.statikk.stats.config.RedisCacheConfig;
import gg.statikk.stats.domain.PlayerGameStats;
import gg.statikk.stats.dto.StatsSummary;
import gg.statikk.stats.event.StatsCalculatedEvent;
import gg.statikk.stats.event.StatsEventPublisher;
import gg.statikk.stats.repository.PlayerGameStatsRepository;
import gg.statikk.stats.repository.StatsPendingRecalcRepository;
import gg.statikk.stats.service.StatsMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;

@Component
public class RecalcWriter implements ItemWriter<PlayerRecalcResult> {

    private static final Logger log = LoggerFactory.getLogger(RecalcWriter.class);

    private final PlayerGameStatsRepository statsRepository;
    private final StatsPendingRecalcRepository pendingRepository;
    private final CacheManager cacheManager;
    private final StatsEventPublisher eventPublisher;

    public RecalcWriter(PlayerGameStatsRepository statsRepository,
                        StatsPendingRecalcRepository pendingRepository,
                        CacheManager cacheManager,
                        StatsEventPublisher eventPublisher) {
        this.statsRepository = statsRepository;
        this.pendingRepository = pendingRepository;
        this.cacheManager = cacheManager;
        this.eventPublisher = eventPublisher;
    }

    /** Corre dentro de la transacción del chunk: o se aplican todos los jugadores del lote, o ninguno. */
    @Override
    public void write(Chunk<? extends PlayerRecalcResult> chunk) {
        for (PlayerRecalcResult result : chunk) {
            Long playerId = result.playerProfileId();
            Instant calculatedAt = Instant.now();

            // Reemplazo total del jugador: así también desaparecen los videojuegos que ya no tienen partidas.
            statsRepository.deleteByPlayerProfileId(playerId);
            List<PlayerGameStats> rows = result.games().stream()
                    .map(game -> PlayerGameStats.from(playerId, game, calculatedAt))
                    .toList();
            statsRepository.saveAll(rows);

            int cleared = pendingRepository.deleteIfUnchanged(playerId, result.version());
            if (cleared == 0) {
                log.info("Llegaron eventos nuevos para el jugador {} mientras se calculaba; "
                        + "queda pendiente para la próxima pasada", playerId);
            }

            StatsSummary overall = StatsMath.overall(result.games());
            runAfterCommit(() -> evictAndNotify(playerId, overall, calculatedAt));
        }
    }

    /**
     * Invalidar la caché y avisar a otros servicios SOLO después del commit. Si se hiciera antes, una lectura
     * concurrente podría volver a cachear datos viejos, o ms-notification recibiría un aviso de algo que
     * todavía no es visible en la base de datos.
     */
    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private void evictAndNotify(Long playerId, StatsSummary overall, Instant calculatedAt) {
        // La transacción ya hizo commit: un fallo aquí no debe tumbar el job, solo registrarse.
        try {
            Cache cache = cacheManager.getCache(RedisCacheConfig.PLAYER_STATS_CACHE);
            if (cache != null) {
                cache.evict(playerId);
            }
        } catch (RuntimeException ex) {
            log.warn("No se pudo invalidar la caché del jugador {} (caducará sola por TTL): {}", playerId, ex.getMessage());
        }
        try {
            eventPublisher.publishStatsCalculated(new StatsCalculatedEvent(
                    playerId, overall.totalMatches(), overall.winRate(), overall.kda(), calculatedAt));
        } catch (RuntimeException ex) {
            log.warn("No se pudo publicar stats.calculated para el jugador {}: {}", playerId, ex.getMessage());
        }
    }
}
