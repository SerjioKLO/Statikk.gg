package com.statikk.player.service.impl;

import com.statikk.player.config.RabbitMQConfig;
import com.statikk.player.dto.MessageResponse;
import com.statikk.player.dto.PlayerGameDto;
import com.statikk.player.dto.RegisterGameRequest;
import com.statikk.player.entity.PlayerGame;
import com.statikk.player.event.GameDeletedEvent;
import com.statikk.player.exception.GameAlreadyRegisteredException;
import com.statikk.player.exception.ResourceNotFoundException;
import com.statikk.player.repository.PlayerGameRepository;
import com.statikk.player.service.PlayerGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerGameServiceImpl implements PlayerGameService {

    private final PlayerGameRepository gameRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * RF-03: Ingresar Videojuego
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "playerGames", key = "#userId"),
            @CacheEvict(value = "playerProfiles", key = "#userId")
    })
    public PlayerGameDto registerGame(Long userId, RegisterGameRequest request) {
        log.info("RF-03: Registering game '{}' for user ID {}", request.getGameName(), userId);

        String trimmedGameName = request.getGameName().trim();

        if (gameRepository.existsByUserIdAndGameNameIgnoreCase(userId, trimmedGameName)) {
            log.warn("Game '{}' is already registered for user ID {}", trimmedGameName, userId);
            throw new GameAlreadyRegisteredException("Game is already registered for this user: " + trimmedGameName);
        }

        PlayerGame game = PlayerGame.builder()
                .userId(userId)
                .gameName(trimmedGameName)
                .genre(request.getGenre() != null ? request.getGenre().trim() : "MOBA")
                .platform(request.getPlatform() != null ? request.getPlatform().trim() : "PC")
                .build();

        PlayerGame saved = gameRepository.save(game);
        log.info("Game '{}' registered successfully with ID: {}", saved.getGameName(), saved.getId());

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "playerGames", key = "#userId")
    public List<PlayerGameDto> getGamesByUserId(Long userId) {
        log.info("Fetching games list for user ID {}", userId);
        return gameRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerGameDto getGameById(Long userId, Long gameId) {
        PlayerGame game = gameRepository.findByIdAndUserId(gameId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with ID: " + gameId + " for user: " + userId));
        return mapToDto(game);
    }

    /**
     * RF-07: Eliminar Registro de Videojuego
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "playerGames", key = "#userId"),
            @CacheEvict(value = "playerProfiles", key = "#userId")
    })
    public MessageResponse deleteGame(Long userId, Long gameId) {
        log.info("RF-07: Request to delete game ID {} for user ID {}", gameId, userId);

        PlayerGame game = gameRepository.findByIdAndUserId(gameId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Videojuego no encontrado en la lista del usuario"));

        String gameName = game.getGameName();

        // 1. Delete relation from database
        gameRepository.delete(game);
        log.info("Game ID {} deleted from database for user ID {}", gameId, userId);

        // 2. Publish asynchronous event via RabbitMQ (RNF-03 Escalabilidad)
        try {
            GameDeletedEvent event = GameDeletedEvent.builder()
                    .userId(userId)
                    .gameId(gameId)
                    .gameName(gameName)
                    .timestamp(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.GAME_DELETED_ROUTING_KEY, event);
            log.info("Published GameDeletedEvent to RabbitMQ for game ID {}", gameId);
        } catch (Exception e) {
            log.error("Failed to publish GameDeletedEvent to RabbitMQ: {}", e.getMessage());
        }

        return MessageResponse.builder()
                .id(gameId)
                .message("Registro eliminado")
                .build();
    }

    private PlayerGameDto mapToDto(PlayerGame game) {
        return PlayerGameDto.builder()
                .id(game.getId())
                .userId(game.getUserId())
                .gameName(game.getGameName())
                .genre(game.getGenre())
                .platform(game.getPlatform())
                .registeredAt(game.getRegisteredAt())
                .build();
    }
}
