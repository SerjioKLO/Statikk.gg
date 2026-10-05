package gg.statikk.player.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "player_games")
public class PlayerGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_profile_id", nullable = false)
    private PlayerProfile playerProfile;

    @Column(name = "game_name", nullable = false, length = 100)
    private String gameName;

    @Column(length = 50)
    private String platform;

    // "rank" es palabra reservada desde MySQL 8.0.2 (window functions), por eso rank_label.
    @Column(name = "rank_label", length = 50)
    private String rankLabel;

    @Column(name = "hours_played", nullable = false)
    private Integer hoursPlayed = 0;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt = Instant.now();

    protected PlayerGame() {
        // requerido por JPA
    }

    public PlayerGame(PlayerProfile playerProfile, String gameName, String platform, String rankLabel, Integer hoursPlayed) {
        this.playerProfile = playerProfile;
        this.gameName = gameName;
        this.platform = platform;
        this.rankLabel = rankLabel;
        this.hoursPlayed = hoursPlayed != null ? hoursPlayed : 0;
    }

    public Long getId() {
        return id;
    }

    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }

    public String getGameName() {
        return gameName;
    }

    public String getPlatform() {
        return platform;
    }

    public String getRankLabel() {
        return rankLabel;
    }

    public Integer getHoursPlayed() {
        return hoursPlayed;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
