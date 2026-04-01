package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.data_aggregation.entity.GithubMonthlyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MonthlyStatsRepository extends JpaRepository<GithubMonthlyStats, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO github_monthly_stats (
            github_id, start_yymm, end_yymm,
            stars, num_of_cr_repos, num_of_co_repos,
            num_of_commits, num_of_PRs, num_of_issues,
            last_updated_at
        ) VALUES (
            :#{#s.githubId}, :#{#s.startYymm}, :#{#s.endYymm},
            0, :#{#s.numOfCrRepos}, :#{#s.numOfCoRepos},
            :#{#s.numOfCommits}, :#{#s.numOfPRs}, :#{#s.numOfIssues},
            NOW()
        )
        ON DUPLICATE KEY UPDATE
            num_of_cr_repos = VALUES(num_of_cr_repos),
            num_of_co_repos = VALUES(num_of_co_repos),
            num_of_commits  = VALUES(num_of_commits),
            num_of_PRs      = VALUES(num_of_PRs),
            num_of_issues   = VALUES(num_of_issues),
            last_updated_at = NOW()
    """, nativeQuery = true)
    void upsert(GithubMonthlyStats s);
}
