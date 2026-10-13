package gg.statikk.stats.service;

import gg.statikk.stats.repository.StatsPendingRecalcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PendingRecalcService {

    private final StatsPendingRecalcRepository pendingRepository;

    public PendingRecalcService(StatsPendingRecalcRepository pendingRepository) {
        this.pendingRepository = pendingRepository;
    }

    @Transactional
    public void markDirty(Long playerProfileId) {
        pendingRepository.markDirty(playerProfileId);
    }
}
