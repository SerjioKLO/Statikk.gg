CREATE TABLE IF NOT EXISTS matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    result VARCHAR(10) NOT NULL,
    duration_minutes DOUBLE,
    hours_played DOUBLE NOT NULL DEFAULT 0.0,
    kills INT DEFAULT 0,
    deaths INT DEFAULT 0,
    assists INT DEFAULT 0,
    champion_or_character VARCHAR(100),
    game_mode VARCHAR(50),
    notes VARCHAR(255),
    played_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_matches_user_id ON matches(user_id);
CREATE INDEX idx_matches_user_game ON matches(user_id, game_id);
CREATE INDEX idx_matches_played_at ON matches(played_at);
