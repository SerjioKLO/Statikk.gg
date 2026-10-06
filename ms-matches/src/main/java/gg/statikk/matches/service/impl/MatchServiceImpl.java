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
        log.info("Match guardada con ID: {}", id);

        publishMatchesUpdateEvent(saved.getUserId(), saved.getGameId(), "CREATED", 1);

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public MatchDto updateMatch(long id, UpdateMatchRequest request){
        log.info("Actualizacion con id: {}", id);

        Matches record = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match no encontrada con ese id: " + id));

        if (request.getResultado() != null) record.setResultado(request.getResultado().trim().toUpperCase());

    }
}
