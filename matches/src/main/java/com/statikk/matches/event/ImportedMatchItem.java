package com.statikk.matches.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportedMatchItem implements Serializable {
    private static final long serialVersionUID = 1L;

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
