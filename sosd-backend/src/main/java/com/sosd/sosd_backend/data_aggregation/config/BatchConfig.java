package com.sosd.sosd_backend.data_aggregation.config;

import com.sosd.sosd_backend.data_aggregation.batch.ContributionStatsProcessor;
import com.sosd.sosd_backend.data_aggregation.batch.ContributionStatsReader;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ContributionStatsReader reader;
    private final ContributionStatsProcessor processor;
    private final ContributionStatsWriter writer;

    @Bean
    public Job contributionStatsJob(Step contributionStatsStep) {
        return new JobBuilder("contributionStatsJob", jobRepository)
                .start(contributionStatsStep)
                .build();
    }

    @Bean
    public Step contributionStatsStep() {
        return new StepBuilder("contributionStatsStep", jobRepository)
                .<GithubAccount, List<GithubContributionStats>>chunk(5, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor())
                .throttleLimit(8)
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("contrib-thread-");
        executor.initialize();
        return executor;
    }
}

