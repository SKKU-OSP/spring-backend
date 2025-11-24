package com.sosd.sosd_backend.data_aggregation.batch;

import com.sosd.sosd_backend.data_aggregation.dto.AccountRepoProjection;
import com.sosd.sosd_backend.data_aggregation.entity.GithubContributionStats;
import com.sosd.sosd_backend.data_aggregation.service.ContributionStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContributionStatsProcessor
        implements ItemProcessor<AccountRepoProjection, List<GithubContributionStats>> {

    private final ContributionStatsService contributionStatsService;

    @Override
    public List<GithubContributionStats> process(AccountRepoProjection dto) {
        return contributionStatsService.calculateStats(dto);
    }
}
