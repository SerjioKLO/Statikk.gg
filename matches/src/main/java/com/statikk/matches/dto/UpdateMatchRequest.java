package com.statikk.matches.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMatchRequest {
    private String result;
    private Double durationMinutes;

    @PositiveOrZero
    private Double hoursPlayed;

    @PositiveOrZero
    private Integer kills;

    @PositiveOrZero
    private Integer deaths;

    @PositiveOrZero
    private Integer assists;

    private String championOrCharacter;
    private String gameMode;
    private String notes;
    private LocalDateTime playedAt;
}
