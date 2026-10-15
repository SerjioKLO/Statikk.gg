package com.statikk.stats.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDto implements Serializable {
    private static final long serialVersionUID = 1L;

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
}
