package com.statikk.matches.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDto {
    private Long id;
    private Long userId;
    private Long gameId;
    private String result;
    private Double durationMinutes;
    private Double hoursPlayed;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private String championOrCharacter;
    private String gameMode;
    private String notes;
    private LocalDateTime playedAt;
    private LocalDateTime createdAt;
}
