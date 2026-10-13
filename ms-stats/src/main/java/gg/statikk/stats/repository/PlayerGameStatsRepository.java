package gg.statikk.stats.repository;

import gg.statikk.stats.domain.PlayerGameStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerGameStatsRepository extends JpaRepository<PlayerGameStats, Long> {

    List<PlayerGameStats> findByPlayerProfileIdOrderByTotalMatchesDesc(Long playerProfileId);

    @Modifying
    @Query("delete from PlayerGameStats s where s.playerProfileId = :playerProfileId")
    int deleteByPlayerProfileId(@Param("playerProfileId") Long playerProfileId);
}
