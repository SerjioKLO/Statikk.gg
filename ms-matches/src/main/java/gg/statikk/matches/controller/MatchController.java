package gg.statikk.matches.controller;

import gg.statikk.matches.dto.CreateMatchRequest;
import gg.statikk.matches.dto.MatchDto;
import gg.statikk.matches.dto.MessageResponse;
import gg.statikk.matches.dto.UpdateMatchRequest;
import gg.statikk.matches.service.MatchService;
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
    public ResponseEntity<MatchDto> createMatch(@Valid @RequestBody CreateMatchRequest request){
        log.info("Creacion de registro manual");
        MatchDto created = matchService.createMatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDto>getMatchById(@PathVariable("id") Long id){
        MatchDto match = matchService.getMatchById(id);
        return ResponseEntity.ok(match);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchDto> updateMatch(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateMatchRequest request){
        log.info("Actualizar match por id: {}", id);
        MatchDto updated = matchService.updateMatch(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteMatch(@PathVariable("id") Long id){
        log.info("Eliminar match por id: {}", id);
        MessageResponse response = matchService.deleteMatch(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MatchDto>> getMatchesByUserId(@PathVariable("userId") Long userId){
        List<MatchDto> matches = matchService.getMatchesByUserId(userId);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/user/{userID}/game/{gameID}")
    public ResponseEntity<MessageResponse> getMatchesByUserAndGame(
            @PathVariable("userID") Long userId,
            @PathVariable("gameID") Long gameId){
        log.info("Eliminar todas las matches por userId {} y gameID {}", userId, gameId);
        MessageResponse response = matchService.deleteMatchesByUserAndGame(userId, gameId);
        return ResponseEntity.ok(response);
    }
}
