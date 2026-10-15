package com.statikk.matches.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMatchRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Game ID is required")
    private Long gameId;

    @NotBlank(message = "Result is required (WIN/LOSS/DRAW)")
    private String result;

    private Double durationMinutes;

    @NotNull(message = "Hours played is required")
    @PositiveOrZero(message = "Hours played cannot be negative")
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
