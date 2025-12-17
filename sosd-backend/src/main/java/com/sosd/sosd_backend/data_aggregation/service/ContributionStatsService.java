package com.sosd.sosd_backend.data_aggregation.service;

import com.sosd.sosd_backend.data_aggregation.dto.AccountRepoProjection;
import com.sosd.sosd_backend.data_aggregation.entity.GithubContributionStats;
import com.sosd.sosd_backend.data_aggregation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionStatsService {

    private final AggregationGithubCommitRepository commitRepository;
    private final AggregationGithubPullRequestRepository prRepository;
    private final AggregationGithubIssueRepository issueRepository;
    private final AggregationGithubContributionStatsRepository statsRepository;

    public List<GithubContributionStats> calculateStats(AccountRepoProjection dto) {
        int currentYear = LocalDate.now().getYear();
        List<GithubContributionStats> results = new ArrayList<>();

        for (int year = 2019; year <= currentYear; year++) {
            final int targetYear = year;

            Long githubId = dto.githubId();
            Long repoId = dto.repoId();

            int commitCount = commitRepository.countByGithubIdAndRepoIdAndYear(githubId, repoId, targetYear);
            int commitLines = commitRepository.sumLinesByGithubIdAndRepoIdAndYear(githubId, repoId, targetYear);
            int prCount = prRepository.countByGithubIdAndRepoIdAndYear(githubId, repoId, targetYear);
            int issueCount = issueRepository.countByGithubIdAndRepoIdAndYear(githubId, repoId, targetYear);

            double commitLineScore = Math.min(commitLines / 7500.0, 1.0);
            double commitCntScore = Math.min(commitCount / 30.0, 1.0);
            double prIssueScore = Math.min((prCount + issueCount) * 0.1, 0.7);
            double guidelineScore =
                    (dto.readme() != null && dto.license() != null && dto.description() != null)
                            ? 0.3 : 0.0;

            double repoScore = commitLineScore + commitCntScore + prIssueScore + guidelineScore;

            GithubContributionStats stats = statsRepository
                    .findByGithubIdAndRepoIdAndYear(githubId, repoId, targetYear)
                    .orElseGet(() -> GithubContributionStats.createNew(githubId, repoId, targetYear));

            stats.updateStats(commitCount, commitLines, prCount, issueCount,
                    guidelineScore, repoScore, dto.star(), dto.fork());

            results.add(stats);
        }


        return results;
    }
}
