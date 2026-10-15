package com.statikk.player.controller;

import com.statikk.player.dto.MessageResponse;
import com.statikk.player.dto.PlayerGameDto;
import com.statikk.player.dto.RegisterGameRequest;
import com.statikk.player.service.PlayerGameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/players/{userId}/games")
@RequiredArgsConstructor
public class PlayerGameController {

    private final PlayerGameService playerGameService;

    /**
     * RF-03: Ingresar Videojuego
     */
    @PostMapping
    public ResponseEntity<PlayerGameDto> registerGame(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody RegisterGameRequest request) {
        log.info("RF-03: Received request to register game for userId: {}", userId);
        PlayerGameDto game = playerGameService.registerGame(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(game);
    }

    /**
     * List all games associated with user
     */
    @GetMapping
    public ResponseEntity<List<PlayerGameDto>> getGames(@PathVariable("userId") Long userId) {
        List<PlayerGameDto> games = playerGameService.getGamesByUserId(userId);
        return ResponseEntity.ok(games);
    }

    /**
     * Get specific game by ID
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<PlayerGameDto> getGame(
            @PathVariable("userId") Long userId,
            @PathVariable("gameId") Long gameId) {
        PlayerGameDto game = playerGameService.getGameById(userId, gameId);
        return ResponseEntity.ok(game);
    }

    /**
     * RF-07: Eliminar Registro de Videojuego
     */
    @DeleteMapping("/{gameId}")
    public ResponseEntity<MessageResponse> deleteGame(
            @PathVariable("userId") Long userId,
            @PathVariable("gameId") Long gameId) {
        log.info("RF-07: Received request to delete game ID {} for userId: {}", gameId, userId);
        MessageResponse response = playerGameService.deleteGame(userId, gameId);
        return ResponseEntity.ok(response);
    }
}
