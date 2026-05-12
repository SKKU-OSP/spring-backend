package com.sosd.sosd_backend.data_aggregation.launcher;

import com.sosd.sosd_backend.data_aggregation.repository.AggregationGithubContributionStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsJobLauncher {

    private final JobLauncher jobLauncher;
    private final Job contributionStatsJob;
    private final Job monthlyStatsJob;
    private final AggregationGithubContributionStatsRepository contributionStatsRepository;

    public void runContributionStatsJob() {
        int deleted = contributionStatsRepository.deleteByPrivateRepos();
        log.info("private 레포 contribution_stats 정리: {}건 삭제", deleted);

        try {
            jobLauncher.run(
                    contributionStatsJob,
                    new JobParametersBuilder()
                            .addLong("timestamp", Instant.now().toEpochMilli())
                            .toJobParameters()
            );
            System.out.println("✅ Contribution Stats Batch Job launched successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runMonthlyStatsJob() {
        try {
            jobLauncher.run(
                    monthlyStatsJob,
                    new JobParametersBuilder()
                            .addLong("timestamp", Instant.now().toEpochMilli())
                            .toJobParameters()
            );
            System.out.println("✅ Monthly Stats Batch Job launched successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
