package com.statikk.matches.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches", indexes = {
        @Index(name = "idx_matches_user_id", columnList = "user_id"),
        @Index(name = "idx_matches_user_game", columnList = "user_id, game_id"),
        @Index(name = "idx_matches_played_at", columnList = "played_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "result", nullable = false, length = 10)
    private String result; // WIN, LOSS, DRAW

    @Column(name = "duration_minutes")
    private Double durationMinutes;

    @Column(name = "hours_played", nullable = false)
    private Double hoursPlayed;

    @Column(name = "kills")
    @Builder.Default
    private Integer kills = 0;

    @Column(name = "deaths")
    @Builder.Default
    private Integer deaths = 0;

    @Column(name = "assists")
    @Builder.Default
    private Integer assists = 0;

    @Column(name = "champion_or_character", length = 100)
    private String championOrCharacter;

    @Column(name = "game_mode", length = 50)
    private String gameMode;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
