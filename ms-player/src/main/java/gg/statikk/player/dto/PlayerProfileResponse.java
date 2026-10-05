package gg.statikk.player.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record PlayerProfileResponse(
        Long id,
        Long userId,
        String displayName,
        String bio,
        String country,
        String avatarUrl,
        Instant createdAt,
        Instant updatedAt,
        List<PlayerGameResponse> games
) implements Serializable {}
