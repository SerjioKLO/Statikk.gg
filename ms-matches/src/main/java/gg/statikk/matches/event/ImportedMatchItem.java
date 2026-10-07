package gg.statikk.matches.event;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportedMatchItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String resultado;
    private Double duracionMinutos;
    private Double horasJugadas;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private String champeonOPersonaje;
    private String modoDeJuego;
    private String notas;
    private LocalDateTime playedAt;
}
