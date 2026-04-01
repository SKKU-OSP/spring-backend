package com.sosd.sosd_backend.data_aggregation.config;

import com.sosd.sosd_backend.data_aggregation.batch.*;
import com.sosd.sosd_backend.data_aggregation.dto.AccountRepoProjection;
import com.sosd.sosd_backend.data_aggregation.entity.GithubContributionStats;
import com.sosd.sosd_backend.data_aggregation.entity.GithubMonthlyStats;
import com.sosd.sosd_backend.entity.github.GithubAccount;
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

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ContributionStatsReader reader;
    private final ContributionStatsProcessor processor;
    private final ContributionStatsWriter writer;
    private final MonthlyStatsReader monthlyStatsReader;
    private final MonthlyStatsProcessor monthlyStatsProcessor;
    private final MonthlyStatsWriter monthlyStatsWriter;

    @Bean
    public Job contributionStatsJob(Step contributionStatsStep) {
        return new JobBuilder("contributionStatsJob", jobRepository)
                .start(contributionStatsStep)
                .build();
    }

//    @Bean
//    public Step contributionStatsStep() {
//        return new StepBuilder("contributionStatsStep", jobRepository)
//                .<GithubAccountRepositoryEntity, List<GithubContributionStats>>chunk(3, transactionManager)
//                .reader(reader)
//                .processor(processor)
//                .writer(writer)
//                .taskExecutor(taskExecutor())
//                .build();
//    }
    @Bean
    public Step contributionStatsStep() {
        return new StepBuilder("contributionStatsStep", jobRepository)
                .<AccountRepoProjection, List<GithubContributionStats>>chunk(3, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job monthlyStatsJob(Step monthlyStatsStep) {
        return new JobBuilder("monthlyStatsJob", jobRepository)
                .start(monthlyStatsStep)
                .build();
    }

    @Bean
    public Step monthlyStatsStep() {
        return new StepBuilder("monthlyStatsStep", jobRepository)
                .<GithubAccount, List<GithubMonthlyStats>>chunk(10, transactionManager)
                .reader(monthlyStatsReader)
                .processor(monthlyStatsProcessor)
                .writer(monthlyStatsWriter)
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

