package gg.statikk.matches.controller;

import gg.statikk.matches.dto.CreateMatchRequest;
import gg.statikk.matches.dto.MatchDto;
import gg.statikk.matches.dto.MessageResponse;
import gg.statikk.matches.dto.UpdateMatchRequest;
import gg.statikk.matches.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(match);
    }
}
