package gg.statikk.stats.batch;

import gg.statikk.stats.repository.StatsPendingRecalcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecalcScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecalcScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job recalculateStatsJob;
    private final StatsPendingRecalcRepository pendingRepository;

    public RecalcScheduler(JobLauncher jobLauncher, Job recalculateStatsJob, StatsPendingRecalcRepository pendingRepository) {
        this.jobLauncher = jobLauncher;
        this.recalculateStatsJob = recalculateStatsJob;
        this.pendingRepository = pendingRepository;
    }

    /** fixedDelay (no fixedRate): la siguiente corrida espera a que termine la anterior, nunca se solapan. */
    @Scheduled(fixedDelayString = "${stats.batch.interval-ms:10000}",
               initialDelayString = "${stats.batch.initial-delay-ms:15000}")
    public void run() {
        // Sin pendientes no se lanza nada: evita llenar las tablas BATCH_* con miles de corridas vacías al día.
        if (pendingRepository.count() == 0) {
            return;
        }
        try {
            // Cada ejecución necesita parámetros únicos; con los mismos, Batch la tomaría por ya ejecutada.
            JobParameters params = new JobParametersBuilder()
                    .addLong("runAt", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(recalculateStatsJob, params);
        } catch (Exception ex) {
            log.error("Falló la corrida del job de recálculo (se reintenta en el próximo ciclo)", ex);
        }
    }
}
