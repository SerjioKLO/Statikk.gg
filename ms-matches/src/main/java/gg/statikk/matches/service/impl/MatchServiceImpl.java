package gg.statikk.matches.service.impl;

import gg.statikk.matches.config.RabbitMQConfig;
import gg.statikk.matches.dto.CreateMatchRequest;
import gg.statikk.matches.dto.MatchDto;
import gg.statikk.matches.dto.MessageResponse;
import gg.statikk.matches.dto.UpdateMatchRequest;
import gg.statikk.matches.entity.Matches;
import gg.statikk.matches.entity.Matches;
import gg.statikk.matches.event.ExcelMatchesImportedEvent;
import gg.statikk.matches.event.ImportedMatchItem;
import gg.statikk.matches.event.MatchesUpdatedEvent;
import gg.statikk.matches.exception.ResourceNotFoundException;
import gg.statikk.matches.repository.MatchRepository;
import gg.statikk.matches.service.MatchService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl {

    private final MatchRepository matchRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public MatchDto createMatch(CreateMatchRequest request){
        log.info("Creacion de registro manual por userId {} y gameId {}", request.getUserId(), request.getGameId());

        Matches record = Matches.builder()
                .userId(request.getUserId())
                .gameId(request.getGameId())
                .resultado(request.getResultado().trim().toUpperCase())
                .duracionMinutos(request.getDuracionMinutos() != null ? request.getDuracionMinutos())
                .horasJugadas(request.getHorasJugadas())
                .kills(request.getKills() != null ? request.getKills() : 0)
                .deatch(request.getDeaths() != null ? request.getDeaths() : 0)
                .assists(request.getAssists() != null ? request.getAssists() : 0)
                .campeonOPersonaje(request.getCampeonOPersonaje())
                .modo_de_juego(request.getModoDeJuego())
                .notas(request.getNotas())
                .playedAt(request.getPlayedAt() != null ? request.getPlayedAt() : LocalDateTime.now())
                .build();

        Matches saved = matchRepository.save(record);
        log.info("Match guardada con ID: {}", saved.getId());

        publishMatchesUpdateEvent(saved.getUserId(), saved.getGameId(), "CREATED", 1);

        return mapToDto(saved);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public MatchDto updateMatch(Long id, UpdateMatchRequest request) {
        log.info("Updating match ID: {}", id);

        Matches record = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + id));

        if (request.getResultado() != null) record.setResultado(request.getResultado().trim().toUpperCase());
        if (request.getDuracionMinutos() != null) record.setDuracionMinutos(request.getDuracionMinutos());
        if (request.getHorasJugadas() != null) record.setHorasJugadas(request.getHorasJugadas());
        if (request.getKills() != null) record.setKills(request.getKills());
        if (request.getDeaths() != null) record.setDeaths(request.getDeaths());
        if (request.getAssists() != null) record.setAssists(request.getAssists());
        if (request.getCampeonOPersonaje() != null) record.setCampeonOPersonaje(request.getCampeonOPersonaje());
        if (request.getModoDeJuego() != null) record.setModoDeJuego(request.getModoDeJuego());
        if (request.getNotas() != null) record.setNotas(request.getNotas());
        if (request.getPlayedAt() != null) record.setPlayedAt(request.getPlayedAt());

        Matches updated = matchRepository.save(record);
        publishMatchesUpdatedEvent(updated.getUserId(), updated.getGameId(), "UPDATED", 1);

        return mapToDto(updated);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public MatchDto getMatchById(Long id) {
        Matches record = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + id));
        return mapToDto(record);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<MatchDto> getMatchesByUserId(Long userId) {
        return matchRepository.findByUserIdOrderByPlayedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<MatchDto> getMatchesByUserAndGame(Long userId, Long gameId) {
        return matchRepository.findByUserIdAndGameIdOrderByPlayedAtDesc(userId, gameId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public MessageResponse deleteMatch(Long id) {
        Matches record = matchRepository.findById(id)
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
    @org.springframework.transaction.annotation.Transactional
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
    @org.springframework.transaction.annotation.Transactional
    public int processImportedMatches(ExcelMatchesImportedEvent event) {
        log.info("Processing {} imported matches for userId {} and gameId {}",
                event.getMatches() != null ? event.getMatches().size() : 0,
                event.getUserId(),
                event.getGameId());

        if (event.getMatches() == null || event.getMatches().isEmpty()) {
            return 0;
        }

        List<Matches> records = event.getMatches().stream()
                .map(item -> Matches.builder()
                        .userId(event.getUserId())
                        .gameId(event.getGameId())
                        .resultado(item.getResultado() != null ? item.getResultado().toUpperCase() : "LOSS")
                        .duracionMinutos(item.getDuracionMinutos())
                        .horasJugadas(item.getHorasJugadas() != null ? item.getHorasJugadas() : 0.0)
                        .kills(item.getKills() != null ? item.getKills() : 0)
                        .deaths(item.getDeaths() != null ? item.getDeaths() : 0)
                        .assists(item.getAssists() != null ? item.getAssists() : 0)
                        .campeonOPersonaje(item.getChampeonOPersonaje())
                        .modoDeJuego(item.getModoDeJuego())
                        .notas(item.getNotas())
                        .playedAt(item.getPlayedAt() != null ? item.getPlayedAt() : LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        List<Matches> savedRecords = matchRepository.saveAll(records);
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

    private MatchDto mapToDto(Matches record) {
        return MatchDto.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .gameId(record.getGameId())
                .resultado(record.getResultado())
                .duracionMinutos(record.getDuracionMinutos())
                .horasJugadas(record.getHorasJugadas())
                .kills(record.getKills())
                .deaths(record.getDeaths())
                .assists(record.getAssists())
                .campeonOPersonaje(record.getCampeonOPersonaje())
                .modoDeJuego(record.getModoDeJuego())
                .notas(record.getNotas())
                .playedAt(record.getPlayedAt())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
