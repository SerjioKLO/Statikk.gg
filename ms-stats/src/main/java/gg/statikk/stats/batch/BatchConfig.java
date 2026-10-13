package gg.statikk.stats.batch;

import gg.statikk.stats.repository.StatsPendingRecalcRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Job "recalculateStatsJob": lee los jugadores pendientes, recalcula sus métricas (processor)
 * y reemplaza su modelo de lectura (writer), en chunks de 20 jugadores por transacción.
 */
@Configuration
public class BatchConfig {

    private static final int CHUNK_SIZE = 20;

    /**
     * @StepScope: se crea una instancia nueva en cada ejecución del step, con una "foto" fresca de los
     * pendientes. Leer de una lista en memoria (y no paginar sobre la tabla) evita que los borrados que
     * hace el writer desplacen las páginas y se salten jugadores.
     */
    @Bean
    @StepScope
    public ItemReader<PendingRecalc> pendingRecalcReader(StatsPendingRecalcRepository pendingRepository) {
        List<PendingRecalc> pending = pendingRepository.findAll().stream()
                .map(p -> new PendingRecalc(p.getPlayerProfileId(), p.getVersion()))
                .toList();
        return new ListItemReader<>(pending);
    }

    @Bean
    public Step recalculateStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                ItemReader<PendingRecalc> pendingRecalcReader,
                                RecalcProcessor processor,
                                RecalcWriter writer) {
        return new StepBuilder("recalculateStep", jobRepository)
                .<PendingRecalc, PlayerRecalcResult>chunk(CHUNK_SIZE, transactionManager)
                .reader(pendingRecalcReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job recalculateStatsJob(JobRepository jobRepository, Step recalculateStep) {
        return new JobBuilder("recalculateStatsJob", jobRepository)
                .start(recalculateStep)
                .build();
    }
}
