package com.statikk.stats.service.impl;

import com.statikk.stats.client.MatchServiceClient;
import com.statikk.stats.config.RabbitMQConfig;
import com.statikk.stats.dto.DashboardSummaryDto;
import com.statikk.stats.dto.MatchDto;
import com.statikk.stats.dto.UserGameStatsDto;
import com.statikk.stats.entity.UserGameStats;
import com.statikk.stats.event.StatsCalculatedEvent;
import com.statikk.stats.exception.ResourceNotFoundException;
import com.statikk.stats.repository.UserGameStatsRepository;
import com.statikk.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final UserGameStatsRepository statsRepository;
    private final MatchServiceClient matchServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userGameStats", key = "#userId + '_' + #gameId")
    public UserGameStatsDto getStatsByUserAndGame(Long userId, Long gameId) {
        log.info("Fetching stats for userId {} and gameId {}", userId, gameId);
        UserGameStats stats = statsRepository.findByUserIdAndGameId(userId, gameId)
                .orElseGet(() -> calculateAndPersistStats(userId, gameId));
        return mapToDto(stats);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userAllStats", key = "#userId")
    public List<UserGameStatsDto> getStatsByUserId(Long userId) {
        log.info("Fetching all game stats for userId {}", userId);
        return statsRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userDashboard", key = "#userId")
    public DashboardSummaryDto getDashboardSummary(Long userId) {
        log.info("RF-05: Generating central dashboard metrics summary for userId {}", userId);
        List<UserGameStats> statsList = statsRepository.findByUserId(userId);

        if (statsList.isEmpty()) {
            return DashboardSummaryDto.builder()
                    .userId(userId)
                    .totalGamesTracked(0)
                    .grandTotalMatches(0)
                    .grandTotalWins(0)
                    .grandTotalLosses(0)
                    .overallWinRate(0.0)
                    .grandTotalHoursPlayed(0.0)
                    .overallAverageKda(0.0)
                    .gameStatsList(List.of())
                    .build();
        }

        int totalMatches = statsList.stream().mapToInt(UserGameStats::getTotalMatches).sum();
        int totalWins = statsList.stream().mapToInt(UserGameStats::getTotalWins).sum();
        int totalLosses = statsList.stream().mapToInt(UserGameStats::getTotalLosses).sum();
        double totalHours = statsList.stream().mapToDouble(UserGameStats::getTotalHoursPlayed).sum();
        int totalKills = statsList.stream().mapToInt(UserGameStats::getTotalKills).sum();
        int totalDeaths = statsList.stream().mapToInt(UserGameStats::getTotalDeaths).sum();
        int totalAssists = statsList.stream().mapToInt(UserGameStats::getTotalAssists).sum();

        double overallWinRate = totalMatches > 0
                ? round((double) totalWins / totalMatches * 100.0, 2)
                : 0.0;

        double overallKda = totalDeaths > 0
                ? round((double) (totalKills + totalAssists) / totalDeaths, 2)
                : round((double) (totalKills + totalAssists), 2);

        List<UserGameStatsDto> dtoList = statsList.stream().map(this::mapToDto).collect(Collectors.toList());

        return DashboardSummaryDto.builder()
                .userId(userId)
                .totalGamesTracked(statsList.size())
                .grandTotalMatches(totalMatches)
                .grandTotalWins(totalWins)
                .grandTotalLosses(totalLosses)
                .overallWinRate(overallWinRate)
                .grandTotalHoursPlayed(round(totalHours, 2))
                .overallAverageKda(overallKda)
                .gameStatsList(dtoList)
                .build();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userGameStats", key = "#userId + '_' + #gameId"),
            @CacheEvict(value = "userAllStats", key = "#userId"),
            @CacheEvict(value = "userDashboard", key = "#userId")
    })
    public UserGameStatsDto recalculateStatsForUserAndGame(Long userId, Long gameId) {
        log.info("Recalculating stats for userId {} and gameId {}", userId, gameId);
        UserGameStats stats = calculateAndPersistStats(userId, gameId);
        return mapToDto(stats);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userGameStats", key = "#userId + '_' + #gameId"),
            @CacheEvict(value = "userAllStats", key = "#userId"),
            @CacheEvict(value = "userDashboard", key = "#userId")
    })
    public void deleteStatsByUserAndGame(Long userId, Long gameId) {
        log.info("RF-07: Deleting stats for userId {} and gameId {}", userId, gameId);
        statsRepository.deleteByUserIdAndGameId(userId, gameId);
    }

    @Override
    @Transactional
    public void recalculateAllStats() {
        log.info("Batch recalculating stats for all tracked entries");
        List<UserGameStats> allStats = statsRepository.findAll();
        for (UserGameStats stats : allStats) {
            calculateAndPersistStats(stats.getUserId(), stats.getGameId());
        }
    }

    private UserGameStats calculateAndPersistStats(Long userId, Long gameId) {
        List<MatchDto> matches = matchServiceClient.getMatchesByUserAndGame(userId, gameId);

        int totalMatches = matches != null ? matches.size() : 0;
        int totalWins = 0;
        int totalLosses = 0;
        int totalDraws = 0;
        double totalHours = 0.0;
        int totalKills = 0;
        int totalDeaths = 0;
        int totalAssists = 0;

        if (matches != null) {
            for (MatchDto m : matches) {
                String res = m.getResult() != null ? m.getResult().toUpperCase() : "LOSS";
                if ("WIN".equals(res)) totalWins++;
                else if ("LOSS".equals(res)) totalLosses++;
                else totalDraws++;

                if (m.getHoursPlayed() != null) totalHours += m.getHoursPlayed();
                if (m.getKills() != null) totalKills += m.getKills();
                if (m.getDeaths() != null) totalDeaths += m.getDeaths();
                if (m.getAssists() != null) totalAssists += m.getAssists();
            }
        }

        double winRate = totalMatches > 0
                ? round((double) totalWins / totalMatches * 100.0, 2)
                : 0.0;

        double averageKda = totalDeaths > 0
                ? round((double) (totalKills + totalAssists) / totalDeaths, 2)
                : round((double) (totalKills + totalAssists), 2);

        UserGameStats stats = statsRepository.findByUserIdAndGameId(userId, gameId)
                .orElseGet(() -> UserGameStats.builder().userId(userId).gameId(gameId).build());

        stats.setTotalMatches(totalMatches);
        stats.setTotalWins(totalWins);
        stats.setTotalLosses(totalLosses);
        stats.setTotalDraws(totalDraws);
        stats.setWinRate(winRate);
        stats.setTotalHoursPlayed(round(totalHours, 2));
        stats.setTotalKills(totalKills);
        stats.setTotalDeaths(totalDeaths);
        stats.setTotalAssists(totalAssists);
        stats.setAverageKda(averageKda);
        stats.setLastCalculatedAt(LocalDateTime.now());

        UserGameStats saved = statsRepository.save(stats);
        log.info("Saved recalculated stats for userId {} and gameId {}: totalMatches={}, winRate={}%",
                userId, gameId, totalMatches, winRate);

        // Publish event to RabbitMQ for ms-notification
        publishStatsCalculatedEvent(saved);

        return saved;
    }

    private void publishStatsCalculatedEvent(UserGameStats stats) {
        try {
            StatsCalculatedEvent event = StatsCalculatedEvent.builder()
                    .userId(stats.getUserId())
                    .gameId(stats.getGameId())
                    .totalMatches(stats.getTotalMatches())
                    .winRate(stats.getWinRate())
                    .totalHoursPlayed(stats.getTotalHoursPlayed())
                    .averageKda(stats.getAverageKda())
                    .timestamp(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.STATS_CALCULATED_ROUTING_KEY, event);
            log.info("Published StatsCalculatedEvent to RabbitMQ for userId {}", stats.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish StatsCalculatedEvent: {}", e.getMessage());
        }
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private UserGameStatsDto mapToDto(UserGameStats stats) {
        return UserGameStatsDto.builder()
                .id(stats.getId())
                .userId(stats.getUserId())
                .gameId(stats.getGameId())
                .totalMatches(stats.getTotalMatches())
                .totalWins(stats.getTotalWins())
                .totalLosses(stats.getTotalLosses())
                .totalDraws(stats.getTotalDraws())
                .winRate(stats.getWinRate())
                .totalHoursPlayed(stats.getTotalHoursPlayed())
                .totalKills(stats.getTotalKills())
                .totalDeaths(stats.getTotalDeaths())
                .totalAssists(stats.getTotalAssists())
                .averageKda(stats.getAverageKda())
                .lastCalculatedAt(stats.getLastCalculatedAt())
                .build();
    }
}
