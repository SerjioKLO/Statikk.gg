CREATE TABLE IF NOT EXISTS player_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL,
    bio VARCHAR(255),
    avatar_url VARCHAR(255),
    primary_region VARCHAR(20) DEFAULT 'LAS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS player_games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    game_name VARCHAR(100) NOT NULL,
    genre VARCHAR(50),
    platform VARCHAR(50),
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_game UNIQUE (user_id, game_name)
);

CREATE INDEX idx_player_profiles_user_id ON player_profiles(user_id);
CREATE INDEX idx_player_games_user_id ON player_games(user_id);
