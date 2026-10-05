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

    private String result;
    private Double duracion_minutos;

    @PositiveOrZero
    private Double horas_jugadas;

    @PositiveOrZero
    private Integer kills;

    @PositiveOrZero
    private Integer deaths;

    @PositiveOrZero
    private Integer assists;

    private String campeon_o_personaje;
    private String modo_de_juego;
    private String notas;
    private LocalDateTime playedAt;
}