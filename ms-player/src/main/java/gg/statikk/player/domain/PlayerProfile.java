package gg.statikk.player.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "player_profiles")
public class PlayerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Id del usuario en ms-auth. No hay FK física entre bases de datos distintas:
    // la relación se mantiene a nivel lógico, vía el header X-User-Id que reenvía el gateway.
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(length = 500)
    private String bio;

    @Column(length = 56)
    private String country;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "playerProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PlayerGame> games = new ArrayList<>();

    protected PlayerProfile() {
        // requerido por JPA
    }

    public PlayerProfile(Long userId, String displayName, String bio, String country, String avatarUrl) {
        this.userId = userId;
        this.displayName = displayName;
        this.bio = bio;
        this.country = country;
        this.avatarUrl = avatarUrl;
    }

    public void update(String displayName, String bio, String country, String avatarUrl) {
        this.displayName = displayName;
        this.bio = bio;
        this.country = country;
        this.avatarUrl = avatarUrl;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBio() {
        return bio;
    }

    public String getCountry() {
        return country;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<PlayerGame> getGames() {
        return games;
    }
}
