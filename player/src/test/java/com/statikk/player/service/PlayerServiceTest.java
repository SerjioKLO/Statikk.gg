package com.statikk.player.service;

import com.statikk.player.dto.CreateProfileRequest;
import com.statikk.player.dto.PlayerGameDto;
import com.statikk.player.dto.PlayerProfileDto;
import com.statikk.player.entity.PlayerProfile;
import com.statikk.player.exception.ResourceNotFoundException;
import com.statikk.player.repository.PlayerProfileRepository;
import com.statikk.player.service.impl.PlayerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerProfileRepository profileRepository;

    @Mock
    private PlayerGameService playerGameService;

    @InjectMocks
    private PlayerServiceImpl playerService;

    private PlayerProfile sampleProfile;

    @BeforeEach
    void setUp() {
        sampleProfile = PlayerProfile.builder()
                .id(1L)
                .userId(10L)
                .nickname("Hide on bush")
                .bio("Mid laner")
                .primaryRegion("KR")
                .build();
    }

    @Test
    @DisplayName("Should create or update player profile")
    void shouldCreateOrUpdateProfile() {
        CreateProfileRequest request = CreateProfileRequest.builder()
                .userId(10L)
                .nickname("Hide on bush")
                .bio("Mid laner")
                .primaryRegion("KR")
                .build();

        when(profileRepository.findByUserId(10L)).thenReturn(Optional.of(sampleProfile));
        when(profileRepository.save(any(PlayerProfile.class))).thenReturn(sampleProfile);
        when(playerGameService.getGamesByUserId(10L)).thenReturn(Collections.emptyList());

        PlayerProfileDto result = playerService.createOrUpdateProfile(request);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getNickname()).isEqualTo("Hide on bush");
        verify(profileRepository, times(1)).save(any(PlayerProfile.class));
    }

    @Test
    @DisplayName("Should fetch profile by userId with games list")
    void shouldGetProfileByUserId() {
        PlayerGameDto gameDto = PlayerGameDto.builder()
                .id(1L)
                .userId(10L)
                .gameName("League of Legends")
                .genre("MOBA")
                .build();

        when(profileRepository.findByUserId(10L)).thenReturn(Optional.of(sampleProfile));
        when(playerGameService.getGamesByUserId(10L)).thenReturn(List.of(gameDto));

        PlayerProfileDto result = playerService.getProfileByUserId(10L);

        assertThat(result).isNotNull();
        assertThat(result.getGames()).hasSize(1);
        assertThat(result.getGames().get(0).getGameName()).isEqualTo("League of Legends");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when profile does not exist")
    void shouldThrowWhenProfileNotFound() {
        when(profileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.getProfileByUserId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Profile not found");
    }
}
