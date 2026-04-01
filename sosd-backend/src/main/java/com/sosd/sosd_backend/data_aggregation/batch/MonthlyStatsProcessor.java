package com.sosd.sosd_backend.data_aggregation.batch;

import com.sosd.sosd_backend.data_aggregation.entity.GithubMonthlyStats;
import com.sosd.sosd_backend.data_aggregation.service.MonthlyStatsService;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyStatsProcessor implements ItemProcessor<GithubAccount, List<GithubMonthlyStats>> {

    private final MonthlyStatsService monthlyStatsService;

    @Override
    public List<GithubMonthlyStats> process(GithubAccount account) {
        return monthlyStatsService.calculateMonthlyStats(account);
    }
}
