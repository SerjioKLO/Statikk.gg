package com.statikk.matches.service.impl;

import com.statikk.matches.config.RabbitMQConfig;
import com.statikk.matches.dto.CreateMatchRequest;
import com.statikk.matches.dto.MatchDto;
import com.statikk.matches.dto.MessageResponse;
import com.statikk.matches.dto.UpdateMatchRequest;
import com.statikk.matches.entity.MatchRecord;
import com.statikk.matches.event.ExcelMatchesImportedEvent;
import com.statikk.matches.event.ImportedMatchItem;
import com.statikk.matches.event.MatchesUpdatedEvent;
import com.statikk.matches.exception.ResourceNotFoundException;
import com.statikk.matches.repository.MatchRepository;
import com.statikk.matches.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public MatchDto createMatch(CreateMatchRequest request) {
        log.info("Creating manual match record for userId {} and gameId {}", request.getUserId(), request.getGameId());

        MatchRecord record = MatchRecord.builder()
                .userId(request.getUserId())
                .gameId(request.getGameId())
                .result(request.getResult().trim().toUpperCase())
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : (request.getHoursPlayed() * 60))
                .hoursPlayed(request.getHoursPlayed())
                .kills(request.getKills() != null ? request.getKills() : 0)
                .deaths(request.getDeaths() != null ? request.getDeaths() : 0)
                .assists(request.getAssists() != null ? request.getAssists() : 0)
                .championOrCharacter(request.getChampionOrCharacter())
                .gameMode(request.getGameMode())
                .notes(request.getNotes())
                .playedAt(request.getPlayedAt() != null ? request.getPlayedAt() : LocalDateTime.now())
                .build();

        MatchRecord saved = matchRepository.save(record);
        log.info("Match saved with ID: {}", saved.getId());

        publishMatchesUpdatedEvent(saved.getUserId(), saved.getGameId(), "CREATED", 1);

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public MatchDto updateMatch(Long id, UpdateMatchRequest request) {
        log.info("Updating match ID: {}", id);

        MatchRecord record = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + id));

        if (request.getResult() != null) record.setResult(request.getResult().trim().toUpperCase());
        if (request.getDurationMinutes() != null) record.setDurationMinutes(request.getDurationMinutes());
        if (request.getHoursPlayed() != null) record.setHoursPlayed(request.getHoursPlayed());
        if (request.getKills() != null) record.setKills(request.getKills());
        if (request.getDeaths() != null) record.setDeaths(request.getDeaths());
        if (request.getAssists() != null) record.setAssists(request.getAssists());
        if (request.getChampionOrCharacter() != null) record.setChampionOrCharacter(request.getChampionOrCharacter());
        if (request.getGameMode() != null) record.setGameMode(request.getGameMode());
        if (request.getNotes() != null) record.setNotes(request.getNotes());
        if (request.getPlayedAt() != null) record.setPlayedAt(request.getPlayedAt());

        MatchRecord updated = matchRepository.save(record);
        publishMatchesUpdatedEvent(updated.getUserId(), updated.getGameId(), "UPDATED", 1);

        return mapToDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchDto getMatchById(Long id) {
        MatchRecord record = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + id));
        return mapToDto(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchDto> getMatchesByUserId(Long userId) {
        return matchRepository.findByUserIdOrderByPlayedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchDto> getMatchesByUserAndGame(Long userId, Long gameId) {
        return matchRepository.findByUserIdAndGameIdOrderByPlayedAtDesc(userId, gameId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageResponse deleteMatch(Long id) {
        MatchRecord record = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + id));

        Long userId = record.getUserId();
        Long gameId = record.getGameId();

        matchRepository.delete(record);
        publishMatchesUpdatedEvent(userId, gameId, "DELETED", 1);

        return MessageResponse.builder()
                .id(id)
                .message("Match deleted successfully")
                .build();
    }

    @Override
    @Transactional
    public MessageResponse deleteMatchesByUserAndGame(Long userId, Long gameId) {
        log.info("Deleting all matches for userId {} and gameId {}", userId, gameId);
        long count = matchRepository.countByUserIdAndGameId(userId, gameId);
        matchRepository.deleteByUserIdAndGameId(userId, gameId);

        publishMatchesUpdatedEvent(userId, gameId, "DELETED", (int) count);

        return MessageResponse.builder()
                .count((int) count)
                .message("All matches deleted for user and game")
                .build();
    }

    @Override
    @Transactional
    public int processImportedMatches(ExcelMatchesImportedEvent event) {
        log.info("Processing {} imported matches for userId {} and gameId {}",
                event.getMatches() != null ? event.getMatches().size() : 0,
                event.getUserId(),
                event.getGameId());

        if (event.getMatches() == null || event.getMatches().isEmpty()) {
            return 0;
        }

        List<MatchRecord> records = event.getMatches().stream()
                .map(item -> MatchRecord.builder()
                        .userId(event.getUserId())
                        .gameId(event.getGameId())
                        .result(item.getResult() != null ? item.getResult().toUpperCase() : "LOSS")
                        .durationMinutes(item.getDurationMinutes())
                        .hoursPlayed(item.getHoursPlayed() != null ? item.getHoursPlayed() : 0.0)
                        .kills(item.getKills() != null ? item.getKills() : 0)
                        .deaths(item.getDeaths() != null ? item.getDeaths() : 0)
                        .assists(item.getAssists() != null ? item.getAssists() : 0)
                        .championOrCharacter(item.getChampionOrCharacter())
                        .gameMode(item.getGameMode())
                        .notes(item.getNotes())
                        .playedAt(item.getPlayedAt() != null ? item.getPlayedAt() : LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        List<MatchRecord> savedRecords = matchRepository.saveAll(records);
        log.info("Persisted {} matches into database", savedRecords.size());

        publishMatchesUpdatedEvent(event.getUserId(), event.getGameId(), "IMPORTED", savedRecords.size());

        return savedRecords.size();
    }

    private void publishMatchesUpdatedEvent(Long userId, Long gameId, String eventType, int count) {
        try {
            MatchesUpdatedEvent event = MatchesUpdatedEvent.builder()
                    .userId(userId)
                    .gameId(gameId)
                    .eventType(eventType)
                    .matchCount(count)
                    .timestamp(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.MATCHES_UPDATED_ROUTING_KEY, event);
            log.info("Published MatchesUpdatedEvent to RabbitMQ: userId={}, gameId={}, type={}", userId, gameId, eventType);
        } catch (Exception e) {
            log.error("Failed to publish MatchesUpdatedEvent: {}", e.getMessage());
        }
    }

    private MatchDto mapToDto(MatchRecord record) {
        return MatchDto.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .gameId(record.getGameId())
                .result(record.getResult())
                .durationMinutes(record.getDurationMinutes())
                .hoursPlayed(record.getHoursPlayed())
                .kills(record.getKills())
                .deaths(record.getDeaths())
                .assists(record.getAssists())
                .championOrCharacter(record.getChampionOrCharacter())
                .gameMode(record.getGameMode())
                .notes(record.getNotes())
                .playedAt(record.getPlayedAt())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
