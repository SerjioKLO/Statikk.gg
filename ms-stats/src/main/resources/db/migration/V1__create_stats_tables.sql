-- Modelo de LECTURA (CQRS): una fila por jugador y videojuego, recalculada desde ms-matches.
CREATE TABLE player_game_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_profile_id BIGINT NOT NULL,
    game_key VARCHAR(100) NOT NULL,
    game_name VARCHAR(100) NOT NULL,
    total_matches INT NOT NULL,
    wins INT NOT NULL,
    losses INT NOT NULL,
    draws INT NOT NULL,
    kills BIGINT NOT NULL,
    deaths BIGINT NOT NULL,
    assists BIGINT NOT NULL,
    total_minutes BIGINT NOT NULL,
    calculated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_player_game (player_profile_id, game_key)
);

-- Jugadores "sucios": los eventos solo los marcan aquí; el job batch los recalcula.
-- "version" sube en cada evento nuevo y evita perder un aviso que llegue mientras se calcula.
CREATE TABLE stats_pending_recalc (
    player_profile_id BIGINT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 1,
    requested_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
