package gg.statikk.matches.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * RabbitMQ entrega "al menos una vez": el mismo mensaje puede llegar dos veces
 * (ej. si el ack se pierde). Guardamos el importId de cada importación procesada
 * para no insertar las mismas partidas duplicadas.
 */
@Entity
@Table(name = "processed_imports")
public class ProcessedImport {

    @Id
    @Column(name = "import_id", length = 36)
    private String importId;

    @Column(name = "player_profile_id", nullable = false)
    private Long playerProfileId;

    @Column(name = "rows_imported", nullable = false)
    private int rowsImported;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt = Instant.now();

    protected ProcessedImport() {
        // requerido por JPA
    }

    public ProcessedImport(String importId, Long playerProfileId, int rowsImported) {
        this.importId = importId;
        this.playerProfileId = playerProfileId;
        this.rowsImported = rowsImported;
    }

    public String getImportId() {
        return importId;
    }

    public Long getPlayerProfileId() {
        return playerProfileId;
    }

    public int getRowsImported() {
        return rowsImported;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
