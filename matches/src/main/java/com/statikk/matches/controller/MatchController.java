package com.statikk.matches.controller;

import com.statikk.matches.dto.CreateMatchRequest;
import com.statikk.matches.dto.MatchDto;
import com.statikk.matches.dto.MessageResponse;
import com.statikk.matches.dto.UpdateMatchRequest;
import com.statikk.matches.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    public ResponseEntity<MatchDto> createMatch(@Valid @RequestBody CreateMatchRequest request) {
        log.info("Creating manual match record");
        MatchDto created = matchService.createMatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDto> getMatchById(@PathVariable("id") Long id) {
        MatchDto match = matchService.getMatchById(id);
        return ResponseEntity.ok(match);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchDto> updateMatch(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateMatchRequest request) {
        log.info("Updating match record ID: {}", id);
        MatchDto updated = matchService.updateMatch(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteMatch(@PathVariable("id") Long id) {
        log.info("Deleting match record ID: {}", id);
        MessageResponse response = matchService.deleteMatch(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MatchDto>> getMatchesByUserId(@PathVariable("userId") Long userId) {
        List<MatchDto> matches = matchService.getMatchesByUserId(userId);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/user/{userId}/game/{gameId}")
    public ResponseEntity<List<MatchDto>> getMatchesByUserAndGame(
            @PathVariable("userId") Long userId,
            @PathVariable("gameId") Long gameId) {
        List<MatchDto> matches = matchService.getMatchesByUserAndGame(userId, gameId);
        return ResponseEntity.ok(matches);
    }

    @DeleteMapping("/user/{userId}/game/{gameId}")
    public ResponseEntity<MessageResponse> deleteMatchesByUserAndGame(
            @PathVariable("userId") Long userId,
            @PathVariable("gameId") Long gameId) {
        log.info("Deleting all matches for userId {} and gameId {}", userId, gameId);
        MessageResponse response = matchService.deleteMatchesByUserAndGame(userId, gameId);
        return ResponseEntity.ok(response);
    }
}
