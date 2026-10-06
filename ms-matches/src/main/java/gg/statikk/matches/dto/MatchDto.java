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
    private Double duracionMinutos;
    private Double horasJugadas;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private String campeonOPersonaje;
    private String modoDeJuego;
    private String notas;
    private LocalDateTime playedAt;
    private LocalDateTime createdAt;
}