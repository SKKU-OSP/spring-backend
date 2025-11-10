package com.sosd.sosd_backend.data_aggregation.batch;

import com.sosd.sosd_backend.data_aggregation.entity.GithubContributionStats;
import com.sosd.sosd_backend.data_aggregation.service.ContributionStatsService;
import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ContributionStatsProcessor implements ItemProcessor<GithubAccountRepositoryEntity, List<GithubContributionStats>> {

    private final ContributionStatsService contributionStatsService;

    @Override
    public List<GithubContributionStats> process(GithubAccountRepositoryEntity link) {
        int currentYear = LocalDate.now().getYear();
        List<GithubContributionStats> results = new ArrayList<>();
        for (int year = 2019; year <= currentYear; year++) {
            GithubContributionStats stat = contributionStatsService.calculateStatsForYear(
                    link.getGithubAccount(), link.getRepository(), year
            );
            results.add(stat);
        }
        return results;
    }
}
