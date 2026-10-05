package gg.statikk.matches.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches", indexes = {
        @Index(name = "idx_matches_user_id", columnList = "user_id"),
        @Index(name = "idx_matches_user_game", columnList = "user_id, game_id"),
        @Index(name = "idx_matches_played_at", columnList = "played_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Matches {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id", nullable = false)
    private long user_id;

    @Column(name = "game_id", nullable = false)
    private long game_id;

    @Column(name = "resultado", nullable = false, length = 10)
    private String resultado;

    @Column(name = "duracion_minutos", nullable = false)
    private Double duracion_minutos;

    @Column(name = "horas_jugadas", nullable = false)
    private Double horas_jugadas;

    @Column(name = "kills")
    private Integer kills = 0;

    @Column(name = "deaths")
    private Integer deaths = 0;

    @Column(name = "assists")
    private Integer assists = 0;

    @Column(name = "campeon_o_personaje", length = 100)
    private String campeon_o_personaje;

    @Column(name = "modo_de_juego", length = 50)
    private String modo_de_juego;

    @Column(name = "notas", length = 255)
    private String notas;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
