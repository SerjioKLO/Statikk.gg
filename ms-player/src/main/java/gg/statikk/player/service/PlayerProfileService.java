package gg.statikk.player.service;

import gg.statikk.player.domain.PlayerGame;
import gg.statikk.player.domain.PlayerProfile;
import gg.statikk.player.dto.CreatePlayerProfileRequest;
import gg.statikk.player.dto.PlayerGameResponse;
import gg.statikk.player.dto.PlayerProfileResponse;
import gg.statikk.player.dto.UpdatePlayerProfileRequest;
import gg.statikk.player.exception.ForbiddenException;
import gg.statikk.player.exception.PlayerNotFoundException;
import gg.statikk.player.exception.ProfileAlreadyExistsException;
import gg.statikk.player.repository.PlayerProfileRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerProfileService {

    private static final String CACHE_NAME = "player-profiles";

    private final PlayerProfileRepository playerProfileRepository;

    public PlayerProfileService(PlayerProfileRepository playerProfileRepository) {
        this.playerProfileRepository = playerProfileRepository;
    }

    @Transactional
    public PlayerProfileResponse create(Long userId, CreatePlayerProfileRequest request) {
        if (playerProfileRepository.existsByUserId(userId)) {
            throw new ProfileAlreadyExistsException(userId);
        }
        PlayerProfile profile = new PlayerProfile(
                userId, request.displayName(), request.bio(), request.country(), request.avatarUrl());
        playerProfileRepository.save(profile);
        return toResponse(profile);
    }

    @Cacheable(cacheNames = CACHE_NAME, key = "#id")
    @Transactional(readOnly = true)
    public PlayerProfileResponse getById(Long id) {
        PlayerProfile profile = findOrThrow(id);
        return toResponse(profile); // dentro de la transacción: inicializa la colección lazy de games
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#id")
    @Transactional
    public PlayerProfileResponse update(Long id, Long requestingUserId, UpdatePlayerProfileRequest request) {
        PlayerProfile profile = findOrThrow(id);
        assertOwner(profile, requestingUserId);
        profile.update(request.displayName(), request.bio(), request.country(), request.avatarUrl());
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    PlayerProfile findOrThrow(Long id) {
        return playerProfileRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    void assertOwner(PlayerProfile profile, Long requestingUserId) {
        if (!profile.getUserId().equals(requestingUserId)) {
            throw new ForbiddenException("No puedes modificar el perfil de otro usuario");
        }
    }

    PlayerProfileResponse toResponse(PlayerProfile profile) {
        List<PlayerGameResponse> games = profile.getGames().stream()
                .map(this::toGameResponse)
                .toList();
        return new PlayerProfileResponse(
                profile.getId(), profile.getUserId(), profile.getDisplayName(), profile.getBio(),
                profile.getCountry(), profile.getAvatarUrl(), profile.getCreatedAt(), profile.getUpdatedAt(), games);
    }

    private PlayerGameResponse toGameResponse(PlayerGame game) {
        return new PlayerGameResponse(
                game.getId(), game.getGameName(), game.getPlatform(), game.getRankLabel(),
                game.getHoursPlayed(), game.getAddedAt());
    }
}
