package gg.statikk.stats.controller;

import gg.statikk.stats.dto.PlayerStatsResponse;
import gg.statikk.stats.service.StatsQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsQueryService statsQueryService;

    public StatsController(StatsQueryService statsQueryService) {
        this.statsQueryService = statsQueryService;
    }

    /** Dashboard de un jugador: métricas globales + desglose por videojuego. */
    @GetMapping("/players/{playerProfileId}")
    public ResponseEntity<PlayerStatsResponse> getPlayerStats(@PathVariable Long playerProfileId) {
        return ResponseEntity.ok(statsQueryService.getPlayerStats(playerProfileId));
    }
}
