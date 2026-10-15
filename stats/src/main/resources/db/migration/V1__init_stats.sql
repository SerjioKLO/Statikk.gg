CREATE TABLE IF NOT EXISTS user_game_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    total_matches INT NOT NULL DEFAULT 0,
    total_wins INT NOT NULL DEFAULT 0,
    total_losses INT NOT NULL DEFAULT 0,
    total_draws INT NOT NULL DEFAULT 0,
    win_rate DOUBLE NOT NULL DEFAULT 0.0,
    total_hours_played DOUBLE NOT NULL DEFAULT 0.0,
    total_kills INT NOT NULL DEFAULT 0,
    total_deaths INT NOT NULL DEFAULT 0,
    total_assists INT NOT NULL DEFAULT 0,
    average_kda DOUBLE NOT NULL DEFAULT 0.0,
    last_calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_stats_user_game UNIQUE (user_id, game_id)
);

CREATE INDEX idx_stats_user_id ON user_game_stats(user_id);
CREATE INDEX idx_stats_user_game ON user_game_stats(user_id, game_id);
