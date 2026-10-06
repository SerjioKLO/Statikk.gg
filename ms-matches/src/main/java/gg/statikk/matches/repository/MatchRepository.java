package gg.statikk.matches.repository;

import gg.statikk.matches.entity.Matches;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Matches, Long> {
    List<Matches> findByUserIdOrderByPlayedAtDesc(Long userId);
    List<Matches> findByUserIdAndGameIdOrderByPlayedAtDesc(Long userId, Long gameId);
    void deleteByUserIdAndGameId(Long userId, Long gameId);
    long countByUserIdAndGameId(Long userId, Long gameId);
}
