package gg.statikk.player.repository;

import gg.statikk.player.domain.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {
    Optional<PlayerProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
