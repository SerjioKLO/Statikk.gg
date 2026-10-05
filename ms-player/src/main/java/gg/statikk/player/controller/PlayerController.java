package gg.statikk.player.controller;

import gg.statikk.player.dto.*;
import gg.statikk.player.service.PlayerGameService;
import gg.statikk.player.service.PlayerProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerProfileService playerProfileService;
    private final PlayerGameService playerGameService;

    public PlayerController(PlayerProfileService playerProfileService, PlayerGameService playerGameService) {
        this.playerProfileService = playerProfileService;
        this.playerGameService = playerGameService;
    }

    @PostMapping
    public ResponseEntity<PlayerProfileResponse> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreatePlayerProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerProfileService.create(userId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(playerProfileService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerProfileResponse> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlayerProfileRequest request) {
        return ResponseEntity.ok(playerProfileService.update(id, userId, request));
    }

    @PostMapping("/{id}/games")
    public ResponseEntity<PlayerGameResponse> addGame(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody AddGameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerGameService.addGame(id, userId, request));
    }

    @GetMapping("/{id}/games")
    public ResponseEntity<List<PlayerGameResponse>> listGames(@PathVariable Long id) {
        return ResponseEntity.ok(playerGameService.listGames(id));
    }

    @DeleteMapping("/{id}/games/{gameId}")
    public ResponseEntity<Void> deleteGame(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @PathVariable Long gameId) {
        playerGameService.deleteGame(id, gameId, userId);
        return ResponseEntity.noContent().build();
    }
}
