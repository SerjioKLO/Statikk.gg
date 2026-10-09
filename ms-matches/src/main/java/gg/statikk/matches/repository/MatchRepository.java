package gg.statikk.matches.repository;

import gg.statikk.matches.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByPlayerProfileIdOrderByPlayedAtDesc(Long playerProfileId);
}
