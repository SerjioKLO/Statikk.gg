package gg.statikk.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePlayerProfileRequest(
        @NotBlank @Size(min = 2, max = 50) String displayName,
        @Size(max = 500) String bio,
        @Size(max = 56) String country,
        String avatarUrl
) {}
