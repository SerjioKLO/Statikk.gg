package com.statikk.player.repository;

import com.statikk.player.entity.PlayerGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerGameRepository extends JpaRepository<PlayerGame, Long> {
    List<PlayerGame> findByUserId(Long userId);
    Optional<PlayerGame> findByIdAndUserId(Long id, Long userId);
    Optional<PlayerGame> findByUserIdAndGameNameIgnoreCase(Long userId, String gameName);
    boolean existsByUserIdAndGameNameIgnoreCase(Long userId, String gameName);
    void deleteByIdAndUserId(Long id, Long userId);
}
