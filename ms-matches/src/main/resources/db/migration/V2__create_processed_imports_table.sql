CREATE TABLE processed_imports (
    import_id VARCHAR(36) PRIMARY KEY,
    player_profile_id BIGINT NOT NULL,
    rows_imported INT NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
