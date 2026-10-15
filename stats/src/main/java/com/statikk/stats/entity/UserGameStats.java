package com.statikk.stats.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_game_stats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "game_id"})
}, indexes = {
        @Index(name = "idx_stats_user_id", columnList = "user_id"),
        @Index(name = "idx_stats_user_game", columnList = "user_id, game_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGameStats implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "total_matches", nullable = false)
    @Builder.Default
    private Integer totalMatches = 0;

    @Column(name = "total_wins", nullable = false)
    @Builder.Default
    private Integer totalWins = 0;

    @Column(name = "total_losses", nullable = false)
    @Builder.Default
    private Integer totalLosses = 0;

    @Column(name = "total_draws", nullable = false)
    @Builder.Default
    private Integer totalDraws = 0;

    @Column(name = "win_rate", nullable = false)
    @Builder.Default
    private Double winRate = 0.0;

    @Column(name = "total_hours_played", nullable = false)
    @Builder.Default
    private Double totalHoursPlayed = 0.0;

    @Column(name = "total_kills", nullable = false)
    @Builder.Default
    private Integer totalKills = 0;

    @Column(name = "total_deaths", nullable = false)
    @Builder.Default
    private Integer totalDeaths = 0;

    @Column(name = "total_assists", nullable = false)
    @Builder.Default
    private Integer totalAssists = 0;

    @Column(name = "average_kda", nullable = false)
    @Builder.Default
    private Double averageKda = 0.0;

    @UpdateTimestamp
    @Column(name = "last_calculated_at", nullable = false)
    private LocalDateTime lastCalculatedAt;
}
