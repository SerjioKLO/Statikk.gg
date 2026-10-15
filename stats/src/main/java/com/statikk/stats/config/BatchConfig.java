package com.statikk.stats.config;

import com.statikk.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final StatsService statsService;

    @Bean
    public Job aggregateStatsJob() {
        return new JobBuilder("aggregateStatsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(aggregateStatsStep())
                .build();
    }

    @Bean
    public Step aggregateStatsStep() {
        return new StepBuilder("aggregateStatsStep", jobRepository)
                .tasklet(statsAggregationTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet statsAggregationTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Executing Spring Batch statsAggregationTasklet - Re-evaluating aggregated metrics");
            statsService.recalculateAllStats();
            return RepeatStatus.FINISHED;
        };
    }
}
