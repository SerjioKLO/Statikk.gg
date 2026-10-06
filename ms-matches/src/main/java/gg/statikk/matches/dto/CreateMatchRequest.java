package gg.statikk.matches.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMatchRequest {

    @NotNull(message = "User ID es requerido")
    private Long userId;

    @NotNull(message = "Game ID es requerido")
    private Long gameId;

    @NotBlank(message = "Resultado es requerido (WIN/LOSS/DRAW)")
    private String resultado;

    private Double duracionMinutos;

    @NotNull(message = "Horas_jugadas es requerido")
    @PositiveOrZero(message = "Horas jugadas no puede ser negativo")
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