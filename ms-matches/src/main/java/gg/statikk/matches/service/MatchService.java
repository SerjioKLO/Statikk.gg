package gg.statikk.matches.service;

import feign.FeignException;
import gg.statikk.matches.client.PlayerClient;
import gg.statikk.matches.client.PlayerProfileClientResponse;
import gg.statikk.matches.domain.Match;
import gg.statikk.matches.dto.CreateMatchRequest;
import gg.statikk.matches.dto.MatchResponse;
import gg.statikk.matches.event.MatchCreatedEvent;
import gg.statikk.matches.event.MatchDeletedEvent;
import gg.statikk.matches.event.MatchEventPublisher;
import gg.statikk.matches.exception.ForbiddenException;
import gg.statikk.matches.exception.MatchNotFoundException;
import gg.statikk.matches.exception.PlayerProfileNotFoundException;
import gg.statikk.matches.exception.PlayerServiceUnavailableException;
import gg.statikk.matches.repository.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final PlayerClient playerClient;
    private final MatchEventPublisher eventPublisher;

    public MatchService(MatchRepository matchRepository, PlayerClient playerClient, MatchEventPublisher eventPublisher) {
        this.matchRepository = matchRepository;
        this.playerClient = playerClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public MatchResponse create(Long requestingUserId, CreateMatchRequest request) {
        PlayerProfileClientResponse profile = fetchProfileOrThrow(request.playerProfileId());
        assertOwner(profile, requestingUserId);

        Match match = new Match(
                request.playerProfileId(), request.gameName(), request.result(), request.durationMinutes(),
                request.kills(), request.deaths(), request.assists(), request.playedAt());
        matchRepository.save(match);

        eventPublisher.publishMatchCreated(new MatchCreatedEvent(
                match.getId(), match.getPlayerProfileId(), match.getGameName(), Instant.now()));

        return toResponse(match);
    }

    @Transactional(readOnly = true)
    public MatchResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> listByPlayer(Long playerProfileId) {
        return matchRepository.findByPlayerProfileIdOrderByPlayedAtDesc(playerProfileId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id, Long requestingUserId) {
        Match match = findOrThrow(id);

        PlayerProfileClientResponse profile = fetchProfileOrThrow(match.getPlayerProfileId());
        assertOwner(profile, requestingUserId);

        matchRepository.delete(match);

        eventPublisher.publishMatchDeleted(new MatchDeletedEvent(
                match.getId(), match.getPlayerProfileId(), Instant.now()));
    }

    /**
     * Tolerancia a fallos frente a ms-player: distingue "el perfil no existe" (404 real,
     * error del cliente) de "ms-player está caído o lento" (503, error de infraestructura),
     * en vez de dejar que cualquier FeignException se convierta en un 500 genérico.
     */
    private PlayerProfileClientResponse fetchProfileOrThrow(Long playerProfileId) {
        try {
            return playerClient.getProfile(playerProfileId);
        } catch (FeignException.NotFound ex) {
            throw new PlayerProfileNotFoundException(playerProfileId);
        } catch (FeignException ex) {
            throw new PlayerServiceUnavailableException(playerProfileId, ex);
        } catch (RuntimeException ex) {
            // Sin instancias disponibles en Eureka, timeouts de conexión, etc.
            throw new PlayerServiceUnavailableException(playerProfileId, ex);
        }
    }

    private void assertOwner(PlayerProfileClientResponse profile, Long requestingUserId) {
        if (!profile.userId().equals(requestingUserId)) {
            throw new ForbiddenException("No puedes operar sobre las partidas de otro jugador");
        }
    }

    private Match findOrThrow(Long id) {
        return matchRepository.findById(id).orElseThrow(() -> new MatchNotFoundException(id));
    }

    private MatchResponse toResponse(Match match) {
        return new MatchResponse(
                match.getId(), match.getPlayerProfileId(), match.getGameName(), match.getResult(),
                match.getDurationMinutes(), match.getKills(), match.getDeaths(), match.getAssists(),
                calculateKda(match), match.getPlayedAt(), match.getCreatedAt());
    }

    /** KDA = (kills + assists) / deaths. Con 0 muertes se usa 1 para no dividir por cero. */
    private double calculateKda(Match match) {
        int deaths = Math.max(match.getDeaths(), 1);
        double kda = (double) (match.getKills() + match.getAssists()) / deaths;
        return Math.round(kda * 100.0) / 100.0;
    }
}
