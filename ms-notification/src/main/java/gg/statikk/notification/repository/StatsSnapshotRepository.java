package gg.statikk.notification.repository;

import gg.statikk.notification.domain.StatsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatsSnapshotRepository extends JpaRepository<StatsSnapshot, Long> {
}
