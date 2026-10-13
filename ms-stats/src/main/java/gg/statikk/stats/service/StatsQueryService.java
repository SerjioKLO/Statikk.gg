package gg.statikk.stats.service;

import gg.statikk.stats.config.RedisCacheConfig;
import gg.statikk.stats.domain.PlayerGameStats;
import gg.statikk.stats.dto.GameStatsResponse;
import gg.statikk.stats.dto.PlayerStatsResponse;
import gg.statikk.stats.repository.PlayerGameStatsRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/** Lado de CONSULTAS (CQRS): solo lee el modelo de lectura ya calculado; nunca toca ms-matches. */
@Service
public class StatsQueryService {

    private final PlayerGameStatsRepository statsRepository;

    public StatsQueryService(PlayerGameStatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    /**
     * Un jugador sin partidas (o cuyo primer cálculo aún está pendiente) devuelve métricas en cero y
     * calculatedAt = null, en vez de 404: ms-stats no es dueño de los perfiles y no sabe si existen.
     * La caché se invalida desde el batch (RecalcWriter) justo después de cada recálculo.
     */
    @Cacheable(cacheNames = RedisCacheConfig.PLAYER_STATS_CACHE, key = "#playerProfileId")
    @Transactional(readOnly = true)
    public PlayerStatsResponse getPlayerStats(Long playerProfileId) {
        List<PlayerGameStats> rows = statsRepository.findByPlayerProfileIdOrderByTotalMatchesDesc(playerProfileId);
        List<GameAggregate> aggregates = rows.stream().map(PlayerGameStats::toAggregate).toList();

        List<GameStatsResponse> games = aggregates.stream()
                .map(g -> new GameStatsResponse(g.gameName(), StatsMath.summarize(g)))
                .toList();

        Instant calculatedAt = rows.stream()
                .map(PlayerGameStats::getCalculatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new PlayerStatsResponse(playerProfileId, StatsMath.overall(aggregates), games, calculatedAt);
    }
}
