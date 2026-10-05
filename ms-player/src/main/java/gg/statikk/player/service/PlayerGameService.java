package gg.statikk.player.service;

import gg.statikk.player.domain.PlayerGame;
import gg.statikk.player.domain.PlayerProfile;
import gg.statikk.player.dto.AddGameRequest;
import gg.statikk.player.dto.PlayerGameResponse;
import gg.statikk.player.event.PlayerEventPublisher;
import gg.statikk.player.event.PlayerGameDeletedEvent;
import gg.statikk.player.exception.GameNotFoundException;
import gg.statikk.player.repository.PlayerGameRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PlayerGameService {

    private static final String CACHE_NAME = "player-profiles";

    private final PlayerGameRepository playerGameRepository;
    private final PlayerProfileService playerProfileService;
    private final PlayerEventPublisher eventPublisher;

    public PlayerGameService(PlayerGameRepository playerGameRepository,
                              PlayerProfileService playerProfileService,
                              PlayerEventPublisher eventPublisher) {
        this.playerGameRepository = playerGameRepository;
        this.playerProfileService = playerProfileService;
        this.eventPublisher = eventPublisher;
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#playerProfileId")
    @Transactional
    public PlayerGameResponse addGame(Long playerProfileId, Long requestingUserId, AddGameRequest request) {
        PlayerProfile profile = playerProfileService.findOrThrow(playerProfileId);
        playerProfileService.assertOwner(profile, requestingUserId);

        PlayerGame game = new PlayerGame(
                profile, request.gameName(), request.platform(), request.rankLabel(), request.hoursPlayed());
        playerGameRepository.save(game);

        return toResponse(game);
    }

    @Transactional(readOnly = true)
    public List<PlayerGameResponse> listGames(Long playerProfileId) {
        playerProfileService.findOrThrow(playerProfileId); // 404 si el perfil no existe
        return playerGameRepository.findByPlayerProfileId(playerProfileId).stream()
                .map(this::toResponse)
                .toList();
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#playerProfileId")
    @Transactional
    public void deleteGame(Long playerProfileId, Long gameId, Long requestingUserId) {
        PlayerProfile profile = playerProfileService.findOrThrow(playerProfileId);
        playerProfileService.assertOwner(profile, requestingUserId);

        PlayerGame game = playerGameRepository.findByIdAndPlayerProfileId(gameId, playerProfileId)
                .orElseThrow(() -> new GameNotFoundException(gameId, playerProfileId));

        playerGameRepository.delete(game);

        eventPublisher.publishGameDeleted(new PlayerGameDeletedEvent(
                playerProfileId, requestingUserId, gameId, game.getGameName(), Instant.now()));
    }

    private PlayerGameResponse toResponse(PlayerGame game) {
        return new PlayerGameResponse(
                game.getId(), game.getGameName(), game.getPlatform(), game.getRankLabel(),
                game.getHoursPlayed(), game.getAddedAt());
    }
}
