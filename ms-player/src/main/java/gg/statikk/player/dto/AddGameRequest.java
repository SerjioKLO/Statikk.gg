package gg.statikk.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record AddGameRequest(
        @NotBlank String gameName,
        String platform,
        String rankLabel,
        @PositiveOrZero Integer hoursPlayed
) {}
