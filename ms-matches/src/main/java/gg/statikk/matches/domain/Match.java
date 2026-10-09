package gg.statikk.matches.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Igual que en ms-player: sin FK física entre bases de datos distintas.
    // La validez de este id se confirma en tiempo real vía Feign contra ms-player.
    @Column(name = "player_profile_id", nullable = false)
    private Long playerProfileId;

    @Column(name = "game_name", nullable = false, length = 100)
    private String gameName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MatchResult result;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer kills = 0;

    @Column(nullable = false)
    private Integer deaths = 0;

    @Column(nullable = false)
    private Integer assists = 0;

    @Column(name = "played_at", nullable = false)
    private Instant playedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Match() {
        // requerido por JPA
    }

    public Match(Long playerProfileId, String gameName, MatchResult result, Integer durationMinutes,
                 Integer kills, Integer deaths, Integer assists, Instant playedAt) {
        this.playerProfileId = playerProfileId;
        this.gameName = gameName;
        this.result = result;
        this.durationMinutes = durationMinutes;
        this.kills = kills != null ? kills : 0;
        this.deaths = deaths != null ? deaths : 0;
        this.assists = assists != null ? assists : 0;
        this.playedAt = playedAt != null ? playedAt : Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getPlayerProfileId() {
        return playerProfileId;
    }

    public String getGameName() {
        return gameName;
    }

    public MatchResult getResult() {
        return result;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Integer getKills() {
        return kills;
    }

    public Integer getDeaths() {
        return deaths;
    }

    public Integer getAssists() {
        return assists;
    }

    public Instant getPlayedAt() {
        return playedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
