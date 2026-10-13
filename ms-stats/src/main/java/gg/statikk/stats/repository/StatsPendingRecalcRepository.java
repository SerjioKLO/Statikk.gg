package gg.statikk.stats.repository;

import gg.statikk.stats.domain.StatsPendingRecalc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StatsPendingRecalcRepository extends JpaRepository<StatsPendingRecalc, Long> {

    /**
     * Upsert atómico: la primera vez inserta con version=1; si ya estaba pendiente, sube la versión.
     * Hacerlo en SQL (y no leer+guardar en Java) evita carreras cuando llegan varios eventos a la vez.
     */
    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO stats_pending_recalc (player_profile_id, version, requested_at)
            VALUES (:playerProfileId, 1, CURRENT_TIMESTAMP(6))
            ON DUPLICATE KEY UPDATE version = version + 1, requested_at = CURRENT_TIMESTAMP(6)
            """)
    void markDirty(@Param("playerProfileId") Long playerProfileId);

    /**
     * Borra el marcador SOLO si nadie lo tocó mientras se calculaba. Si llegó otro evento (version distinta),
     * el marcador sobrevive y el jugador se recalcula otra vez en la próxima pasada: no se pierde ningún aviso.
     */
    @Modifying
    @Query("delete from StatsPendingRecalc p where p.playerProfileId = :playerProfileId and p.version = :version")
    int deleteIfUnchanged(@Param("playerProfileId") Long playerProfileId, @Param("version") long version);
}
