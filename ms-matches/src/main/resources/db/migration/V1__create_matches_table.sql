CREATE TABLE matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_profile_id BIGINT NOT NULL,
    game_name VARCHAR(100) NOT NULL,
    result VARCHAR(10) NOT NULL,
    duration_minutes INT,
    kills INT NOT NULL DEFAULT 0,
    deaths INT NOT NULL DEFAULT 0,
    assists INT NOT NULL DEFAULT 0,
    played_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_matches_player_profile (player_profile_id)
);
