package gg.statikk.matches.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDto {

    private long id;
    private Long userId;
    private Long gameId;
    private String resultado;
    private Double duracion_minutos;
    private Double horas_jugadas;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private String campeon_o_personaje;
    private String modo_de_juego;
    private String notas;
    private LocalDateTime playedAt;
    private LocalDateTime createdAt;
}