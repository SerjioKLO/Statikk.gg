package gg.statikk.matches.repository;

import gg.statikk.matches.domain.ProcessedImport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedImportRepository extends JpaRepository<ProcessedImport, String> {
}
