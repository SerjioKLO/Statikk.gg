package gg.statikk.matches.controller;

import gg.statikk.matches.dto.CreateMatchRequest;
import gg.statikk.matches.dto.MatchResponse;
import gg.statikk.matches.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    public ResponseEntity<MatchResponse> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateMatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.create(userId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<MatchResponse>> listByPlayer(@RequestParam Long playerProfileId) {
        return ResponseEntity.ok(matchService.listByPlayer(playerProfileId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        matchService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
