package gg.statikk.matches.service;

import gg.statikk.matches.repository.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchCleanupService {

    private final MatchRepository matchRepository;

    public MatchCleanupService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    /** @return cuántas partidas se borraron (0 si ya no había ninguna: el borrado es idempotente). */
    @Transactional
    public int deleteMatchesOfGame(Long playerProfileId, String gameName) {
        return matchRepository.deleteByPlayerAndGame(playerProfileId, gameName);
    }
}
