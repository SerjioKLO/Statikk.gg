package gg.statikk.notification.domain;

import gg.statikk.notification.service.StatsView;
import jakarta.persistence.*;

import java.time.Instant;

/** Últimas métricas ya notificadas de un jugador. Sirve para no repetir avisos y para mostrar la variación. */
@Entity
@Table(name = "stats_snapshots")
public class StatsSnapshot {

    @Id
    @Column(name = "player_profile_id")
    private Long playerProfileId;

    @Column(name = "total_matches", nullable = false)
    private int totalMatches;

    @Column(name = "win_rate", nullable = false)
    private double winRate;

    @Column(nullable = false)
    private double kda;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected StatsSnapshot() {
        // requerido por JPA
    }

    public StatsSnapshot(Long playerProfileId) {
        this.playerProfileId = playerProfileId;
    }

    public void update(StatsView view) {
        this.totalMatches = view.totalMatches();
        this.winRate = view.winRate();
        this.kda = view.kda();
        this.updatedAt = Instant.now();
    }

    public StatsView toView() {
        return new StatsView(totalMatches, winRate, kda);
    }

    public Long getPlayerProfileId() {
        return playerProfileId;
    }
}
