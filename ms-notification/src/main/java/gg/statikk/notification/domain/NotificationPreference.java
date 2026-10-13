package gg.statikk.notification.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(length = 120)
    private String email;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected NotificationPreference() {
        // requerido por JPA
    }

    public NotificationPreference(Long userId) {
        this.userId = userId;
    }

    public void update(boolean emailEnabled, String email) {
        this.emailEnabled = emailEnabled;
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public String getEmail() {
        return email;
    }
}
