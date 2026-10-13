package gg.statikk.stats.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * "Este jugador cambió y hay que recalcularlo". Una fila por jugador, no una por evento:
 * 5000 partidas importadas generan un solo recálculo, no 5000.
 */
@Entity
@Table(name = "stats_pending_recalc")
public class StatsPendingRecalc {

    @Id
    @Column(name = "player_profile_id")
    private Long playerProfileId;

    // Se incrementa en cada evento nuevo (ver markDirty). No es el @Version de JPA a propósito.
    @Column(nullable = false)
    private long version;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    protected StatsPendingRecalc() {
        // requerido por JPA
    }

    public Long getPlayerProfileId() {
        return playerProfileId;
    }

    public long getVersion() {
        return version;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }
}
