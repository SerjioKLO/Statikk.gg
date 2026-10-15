package com.statikk.player.controller;

import com.statikk.player.dto.CreateProfileRequest;
import com.statikk.player.dto.PlayerProfileDto;
import com.statikk.player.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("/profile")
    public ResponseEntity<PlayerProfileDto> createOrUpdateProfile(@Valid @RequestBody CreateProfileRequest request) {
        log.info("Creating/updating profile for user: {}", request.getUserId());
        PlayerProfileDto profile = playerService.createOrUpdateProfile(request);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<PlayerProfileDto> getProfile(@PathVariable("userId") Long userId) {
        log.info("Fetching profile for user: {}", userId);
        PlayerProfileDto profile = playerService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }
}
