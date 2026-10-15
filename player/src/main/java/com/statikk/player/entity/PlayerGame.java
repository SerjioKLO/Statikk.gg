package com.statikk.player.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_games", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "game_name"})
}, indexes = {
        @Index(name = "idx_user_game", columnList = "user_id, game_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerGame implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "game_name", nullable = false, length = 100)
    private String gameName;

    @Column(length = 50)
    private String genre;

    @Column(length = 50)
    private String platform;

    @CreationTimestamp
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;
}
