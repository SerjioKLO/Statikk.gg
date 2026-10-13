CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    read_at TIMESTAMP(6) NULL,
    INDEX idx_notifications_user_created (user_id, created_at)
);

-- Correo OPT-IN: por defecto nadie recibe correos hasta que lo activa en sus preferencias.
CREATE TABLE notification_preferences (
    user_id BIGINT PRIMARY KEY,
    email_enabled BIT NOT NULL DEFAULT 0,
    email VARCHAR(120) NULL,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

-- Últimas métricas notificadas por jugador: permite no avisar si nada cambió y mostrar la variación.
CREATE TABLE stats_snapshots (
    player_profile_id BIGINT PRIMARY KEY,
    total_matches INT NOT NULL,
    win_rate DOUBLE NOT NULL,
    kda DOUBLE NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
