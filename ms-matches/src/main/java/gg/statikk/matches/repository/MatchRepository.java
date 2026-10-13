package gg.statikk.matches.repository;

import gg.statikk.matches.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByPlayerProfileIdOrderByPlayedAtDesc(Long playerProfileId);

    /** Una sola sentencia DELETE (no una por partida). Compara sin distinguir mayúsculas, igual que ms-stats. */
    @Modifying
    @Query("delete from Match m where m.playerProfileId = :playerProfileId and lower(m.gameName) = lower(:gameName)")
    int deleteByPlayerAndGame(@Param("playerProfileId") Long playerProfileId, @Param("gameName") String gameName);
}
