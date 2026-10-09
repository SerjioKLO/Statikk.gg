package gg.statikk.matches.dto;

import gg.statikk.matches.domain.MatchResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public record CreateMatchRequest(
        @NotNull Long playerProfileId,
        @NotBlank String gameName,
        @NotNull MatchResult result,
        @PositiveOrZero Integer durationMinutes,
        @PositiveOrZero Integer kills,
        @PositiveOrZero Integer deaths,
        @PositiveOrZero Integer assists,
        Instant playedAt
) {}
