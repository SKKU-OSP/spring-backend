package com.sosd.sosd_backend.data_aggregation.service;


import com.sosd.sosd_backend.data_aggregation.entity.GithubContributionStats;
import com.sosd.sosd_backend.data_aggregation.repository.*;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionStatsService {

    private final GithubCommitRepository commitRepository;
    private final GithubPullRequestRepository prRepository;
    private final GithubIssueRepository issueRepository;
    private final GithubContributionStatsRepository statsRepository;

    public GithubContributionStats calculateStatsForYear(GithubAccount account,
                                                         GithubRepositoryEntity repo,
                                                         int year) {
        Long githubId = account.getGithubId();
        Long repoId = repo.getId();

        // 통계 데이터 수집
        int commitCount = commitRepository.countByGithubIdAndRepoIdAndYear(githubId, repoId, year);
        int commitLines = commitRepository.sumLinesByGithubIdAndRepoIdAndYear(githubId, repoId, year);
        int prCount = prRepository.countByGithubIdAndRepoIdAndYear(githubId, repoId, year);
        int issueCount = issueRepository.countByGithubIdAndRepoIdAndYear(githubId, repoId, year);

        // 점수 계산
        double commitLineScore = Math.min(commitLines / 7500.0, 1.0);
        double commitCntScore = Math.min(commitCount / 30.0, 1.0);
        double prIssueScore = Math.min((prCount + issueCount) * 0.1, 0.7);
        double guidelineScore =
                (repo.getReadme() != null && repo.getLicense() != null && repo.getDescription() != null)
                        ? 0.3 : 0.0;

        double repoScore = commitLineScore + commitCntScore + prIssueScore + guidelineScore;

        // 기존 데이터 조회 (없으면 새로 생성)
        GithubContributionStats stats = statsRepository
                .findByGithubIdAndRepoIdAndYear(githubId, repoId, year)
                .orElseGet(() -> GithubContributionStats.createNew(githubId, repoId, year));

        // 도메인 메서드로 갱신
        stats.updateStats(commitCount, commitLines, prCount, issueCount,
                guidelineScore, repoScore, repo.getStar(), repo.getFork());

        // 저장 후 반환
        return statsRepository.save(stats);
    }
}


