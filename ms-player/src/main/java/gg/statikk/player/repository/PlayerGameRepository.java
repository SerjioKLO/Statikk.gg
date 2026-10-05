package gg.statikk.player.repository;

import gg.statikk.player.domain.PlayerGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerGameRepository extends JpaRepository<PlayerGame, Long> {
    List<PlayerGame> findByPlayerProfileId(Long playerProfileId);
    Optional<PlayerGame> findByIdAndPlayerProfileId(Long id, Long playerProfileId);
}
