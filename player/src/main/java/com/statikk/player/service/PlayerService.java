package com.statikk.player.service;

import com.statikk.player.dto.CreateProfileRequest;
import com.statikk.player.dto.PlayerProfileDto;

public interface PlayerService {
    PlayerProfileDto createOrUpdateProfile(CreateProfileRequest request);
    PlayerProfileDto getProfileByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
