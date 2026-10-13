package gg.statikk.stats.domain;

import gg.statikk.stats.service.GameAggregate;
import jakarta.persistence.*;

import java.time.Instant;

/** Modelo de lectura: totales de un jugador en un videojuego. Nunca se edita; se reemplaza al recalcular. */
@Entity
@Table(name = "player_game_stats")
public class PlayerGameStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_profile_id", nullable = false)
    private Long playerProfileId;

    @Column(name = "game_key", nullable = false, length = 100)
    private String gameKey;

    @Column(name = "game_name", nullable = false, length = 100)
    private String gameName;

    @Column(name = "total_matches", nullable = false)
    private int totalMatches;

    @Column(nullable = false)
    private int wins;

    @Column(nullable = false)
    private int losses;

    @Column(nullable = false)
    private int draws;

    @Column(nullable = false)
    private long kills;

    @Column(nullable = false)
    private long deaths;

    @Column(nullable = false)
    private long assists;

    @Column(name = "total_minutes", nullable = false)
    private long totalMinutes;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    protected PlayerGameStats() {
        // requerido por JPA
    }

    public static PlayerGameStats from(Long playerProfileId, GameAggregate g, Instant calculatedAt) {
        PlayerGameStats s = new PlayerGameStats();
        s.playerProfileId = playerProfileId;
        s.gameKey = g.gameKey();
        s.gameName = g.gameName();
        s.totalMatches = g.totalMatches();
        s.wins = g.wins();
        s.losses = g.losses();
        s.draws = g.draws();
        s.kills = g.kills();
        s.deaths = g.deaths();
        s.assists = g.assists();
        s.totalMinutes = g.totalMinutes();
        s.calculatedAt = calculatedAt;
        return s;
    }

    public GameAggregate toAggregate() {
        return new GameAggregate(gameKey, gameName, totalMatches, wins, losses, draws, kills, deaths, assists, totalMinutes);
    }

    public Long getId() {
        return id;
    }

    public Long getPlayerProfileId() {
        return playerProfileId;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}
