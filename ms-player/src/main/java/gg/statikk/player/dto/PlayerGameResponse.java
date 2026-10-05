package gg.statikk.player.dto;

import java.io.Serializable;
import java.time.Instant;

public record PlayerGameResponse(
        Long id,
        String gameName,
        String platform,
        String rankLabel,
        Integer hoursPlayed,
        Instant addedAt
) implements Serializable {}
