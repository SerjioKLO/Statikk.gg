CREATE TABLE player_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    display_name VARCHAR(50) NOT NULL,
    bio VARCHAR(500),
    country VARCHAR(56),
    avatar_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE player_games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_profile_id BIGINT NOT NULL,
    game_name VARCHAR(100) NOT NULL,
    platform VARCHAR(50),
    rank_label VARCHAR(50),
    hours_played INT NOT NULL DEFAULT 0,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_player_games_profile FOREIGN KEY (player_profile_id)
        REFERENCES player_profiles(id) ON DELETE CASCADE
);
