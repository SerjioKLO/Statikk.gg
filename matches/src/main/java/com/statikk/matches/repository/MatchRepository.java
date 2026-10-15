package com.statikk.matches.repository;

import com.statikk.matches.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<MatchRecord, Long> {
    List<MatchRecord> findByUserIdOrderByPlayedAtDesc(Long userId);
    List<MatchRecord> findByUserIdAndGameIdOrderByPlayedAtDesc(Long userId, Long gameId);
    void deleteByUserIdAndGameId(Long userId, Long gameId);
    long countByUserIdAndGameId(Long userId, Long gameId);
}
