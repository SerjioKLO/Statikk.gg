package com.statikk.player.service.impl;

import com.statikk.player.dto.CreateProfileRequest;
import com.statikk.player.dto.PlayerGameDto;
import com.statikk.player.dto.PlayerProfileDto;
import com.statikk.player.entity.PlayerProfile;
import com.statikk.player.exception.ResourceNotFoundException;
import com.statikk.player.repository.PlayerProfileRepository;
import com.statikk.player.service.PlayerGameService;
import com.statikk.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerProfileRepository profileRepository;
    private final PlayerGameService playerGameService;

    @Override
    @Transactional
    @CacheEvict(value = "playerProfiles", key = "#request.userId")
    public PlayerProfileDto createOrUpdateProfile(CreateProfileRequest request) {
        log.info("Creating/updating profile for userId: {}", request.getUserId());

        PlayerProfile profile = profileRepository.findByUserId(request.getUserId())
                .orElseGet(() -> PlayerProfile.builder().userId(request.getUserId()).build());

        profile.setNickname(request.getNickname());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getPrimaryRegion() != null) profile.setPrimaryRegion(request.getPrimaryRegion());

        PlayerProfile saved = profileRepository.save(profile);
        List<PlayerGameDto> games = playerGameService.getGamesByUserId(request.getUserId());

        return mapToDto(saved, games);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "playerProfiles", key = "#userId")
    public PlayerProfileDto getProfileByUserId(Long userId) {
        log.info("Fetching profile from database for userId: {}", userId);

        PlayerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for userId: " + userId));

        List<PlayerGameDto> games = playerGameService.getGamesByUserId(userId);
        return mapToDto(profile, games);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        return profileRepository.existsByUserId(userId);
    }

    private PlayerProfileDto mapToDto(PlayerProfile profile, List<PlayerGameDto> games) {
        return PlayerProfileDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .nickname(profile.getNickname())
                .bio(profile.getBio())
                .avatarUrl(profile.getAvatarUrl())
                .primaryRegion(profile.getPrimaryRegion())
                .games(games)
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
