package gg.statikk.matches.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMatchRequest {

    private String resultado;
    private Double duracionMinutos;

    @PositiveOrZero
    private Double horasJugadas;

    @PositiveOrZero
    private Integer kills;

    @PositiveOrZero
    private Integer deaths;

    @PositiveOrZero
    private Integer assists;

    private String campeonOPersonaje;
    private String modoDeJuego;
    private String notas;
    private LocalDateTime playedAt;
}