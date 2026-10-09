package gg.statikk.matches.dto;

import gg.statikk.matches.domain.MatchResult;

import java.time.Instant;

public record MatchResponse(
        Long id,
        Long playerProfileId,
        String gameName,
        MatchResult result,
        Integer durationMinutes,
        Integer kills,
        Integer deaths,
        Integer assists,
        double kda,
        Instant playedAt,
        Instant createdAt
) {}
