package com.statikk.excel_parser.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchImportDto {
    private String result; // WIN / LOSS / DRAW
    private Double durationMinutes;
    private Double hoursPlayed;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private String championOrCharacter;
    private String gameMode;
    private String notes;
    private LocalDateTime playedAt;
}
