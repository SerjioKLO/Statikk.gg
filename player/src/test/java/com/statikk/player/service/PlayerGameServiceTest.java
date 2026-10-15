package com.statikk.player.service;

import com.statikk.player.config.RabbitMQConfig;
import com.statikk.player.dto.MessageResponse;
import com.statikk.player.dto.PlayerGameDto;
import com.statikk.player.dto.RegisterGameRequest;
import com.statikk.player.entity.PlayerGame;
import com.statikk.player.event.GameDeletedEvent;
import com.statikk.player.exception.GameAlreadyRegisteredException;
import com.statikk.player.exception.ResourceNotFoundException;
import com.statikk.player.repository.PlayerGameRepository;
import com.statikk.player.service.impl.PlayerGameServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerGameServiceTest {

    @Mock
    private PlayerGameRepository gameRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PlayerGameServiceImpl playerGameService;

    private PlayerGame sampleGame;

    @BeforeEach
    void setUp() {
        sampleGame = PlayerGame.builder()
                .id(1L)
                .userId(5L)
                .gameName("Valorant")
                .genre("FPS")
                .platform("PC")
                .registeredAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("RF-03: Should register a new videogame successfully")
    void shouldRegisterGameSuccessfully() {
        RegisterGameRequest request = RegisterGameRequest.builder()
                .gameName("Valorant")
                .genre("FPS")
                .platform("PC")
                .build();

        when(gameRepository.existsByUserIdAndGameNameIgnoreCase(5L, "Valorant")).thenReturn(false);
        when(gameRepository.save(any(PlayerGame.class))).thenReturn(sampleGame);

        PlayerGameDto result = playerGameService.registerGame(5L, request);

        assertThat(result).isNotNull();
        assertThat(result.getGameName()).isEqualTo("Valorant");
        verify(gameRepository, times(1)).save(any(PlayerGame.class));
    }

    @Test
    @DisplayName("RF-03 Exception: Should throw GameAlreadyRegisteredException on duplicate game")
    void shouldThrowWhenDuplicateGame() {
        RegisterGameRequest request = RegisterGameRequest.builder()
                .gameName("Valorant")
                .build();

        when(gameRepository.existsByUserIdAndGameNameIgnoreCase(5L, "Valorant")).thenReturn(true);

        assertThatThrownBy(() -> playerGameService.registerGame(5L, request))
                .isInstanceOf(GameAlreadyRegisteredException.class)
                .hasMessageContaining("Game is already registered");

        verify(gameRepository, never()).save(any(PlayerGame.class));
    }

    @Test
    @DisplayName("RF-07: Should delete videogame and publish GameDeletedEvent to RabbitMQ")
    void shouldDeleteGameAndPublishEvent() {
        when(gameRepository.findByIdAndUserId(1L, 5L)).thenReturn(Optional.of(sampleGame));

        MessageResponse response = playerGameService.deleteGame(5L, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Registro eliminado");
        assertThat(response.getId()).isEqualTo(1L);

        verify(gameRepository, times(1)).delete(sampleGame);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.GAME_DELETED_ROUTING_KEY),
                any(GameDeletedEvent.class)
        );
    }

    @Test
    @DisplayName("RF-07 Exception: Should throw ResourceNotFoundException when game to delete does not exist")
    void shouldThrowWhenDeletingNonExistingGame() {
        when(gameRepository.findByIdAndUserId(99L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerGameService.deleteGame(5L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(gameRepository, never()).delete(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}
