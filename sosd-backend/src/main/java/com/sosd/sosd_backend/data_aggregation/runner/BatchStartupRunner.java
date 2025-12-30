package com.sosd.sosd_backend.data_aggregation.runner;

import com.sosd.sosd_backend.data_aggregation.launcher.StatsJobLauncher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchStartupRunner implements CommandLineRunner {

    private final StatsJobLauncher statsJobLauncher;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Starting Contribution Stats Batch Job...");
        statsJobLauncher.runContributionStatsJob();
        System.out.println("✅ Contribution Stats Batch Job completed!");
    }
}
