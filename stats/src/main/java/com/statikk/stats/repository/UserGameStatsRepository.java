package com.statikk.stats.repository;

import com.statikk.stats.entity.UserGameStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGameStatsRepository extends JpaRepository<UserGameStats, Long> {
    List<UserGameStats> findByUserId(Long userId);
    Optional<UserGameStats> findByUserIdAndGameId(Long userId, Long gameId);
    void deleteByUserIdAndGameId(Long userId, Long gameId);
}
