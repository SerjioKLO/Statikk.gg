package com.statikk.stats.controller;

import com.statikk.stats.dto.DashboardSummaryDto;
import com.statikk.stats.dto.UserGameStatsDto;
import com.statikk.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final JobLauncher jobLauncher;
    private final Job aggregateStatsJob;

    /**
     * RF-05: Visualizar Métricas y Promedios (Juego específico)
     */
    @GetMapping("/user/{userId}/game/{gameId}")
    public ResponseEntity<UserGameStatsDto> getStatsByUserAndGame(
            @PathVariable("userId") Long userId,
            @PathVariable("gameId") Long gameId) {
        log.info("RF-05: Fetching game stats for user {} and game {}", userId, gameId);
        UserGameStatsDto stats = statsService.getStatsByUserAndGame(userId, gameId);
        return ResponseEntity.ok(stats);
    }

    /**
     * List all game stats for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserGameStatsDto>> getStatsByUserId(@PathVariable("userId") Long userId) {
        List<UserGameStatsDto> stats = statsService.getStatsByUserId(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * RF-05: Visualizar Métricas y Promedios (Panel Centralizado Completo)
     */
    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(@PathVariable("userId") Long userId) {
        log.info("RF-05: Generating central dashboard metrics for user {}", userId);
        DashboardSummaryDto dashboard = statsService.getDashboardSummary(userId);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * RF-06: Actualizar Estadísticas
     */
    @PostMapping("/recalculate/user/{userId}/game/{gameId}")
    public ResponseEntity<UserGameStatsDto> recalculateStats(
            @PathVariable("userId") Long userId,
            @PathVariable("gameId") Long gameId) {
        log.info("RF-06: Manual trigger to recalculate stats for user {} and game {}", userId, gameId);
        UserGameStatsDto recalculated = statsService.recalculateStatsForUserAndGame(userId, gameId);
        return ResponseEntity.ok(recalculated);
    }

    /**
     * Spring Batch Job Trigger
     */
    @PostMapping("/batch/run")
    public ResponseEntity<Map<String, String>> runBatchJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(aggregateStatsJob, params);
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Batch stats aggregation job launched successfully"));
        } catch (Exception e) {
            log.error("Failed to run batch aggregation job: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }
}
