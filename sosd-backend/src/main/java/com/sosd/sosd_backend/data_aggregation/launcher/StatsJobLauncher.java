package com.sosd.sosd_backend.data_aggregation.launcher;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class StatsJobLauncher {

    private final JobLauncher jobLauncher;
    private final Job contributionStatsJob;

    public void runContributionStatsJob() {
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
}
