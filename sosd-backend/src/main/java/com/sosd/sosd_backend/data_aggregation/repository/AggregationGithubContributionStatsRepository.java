package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.data_aggregation.entity.GithubContributionStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AggregationGithubContributionStatsRepository extends JpaRepository<GithubContributionStats, Long> {
    Optional<GithubContributionStats> findByGithubIdAndRepoIdAndYear(Long githubId, Long repoId, int year);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO github_contribution_stats (
            github_id, repo_id, year,
            commit_count, commit_lines, pr_count, issue_count,
            guideline_score, repo_score, star_count, fork_count,
            last_updated_at
        ) VALUES (
            :#{#s.githubId}, :#{#s.repoId}, :#{#s.year},
            :#{#s.commitCount}, :#{#s.commitLines}, :#{#s.prCount}, :#{#s.issueCount},
            :#{#s.guidelineScore}, :#{#s.repoScore}, :#{#s.starCount}, :#{#s.forkCount},
            NOW()
        )
        ON DUPLICATE KEY UPDATE
            commit_count = VALUES(commit_count),
            commit_lines = VALUES(commit_lines),
            pr_count = VALUES(pr_count),
            issue_count = VALUES(issue_count),
            guideline_score = VALUES(guideline_score),
            repo_score = VALUES(repo_score),
            star_count = VALUES(star_count),
            fork_count = VALUES(fork_count),
            last_updated_at = NOW()
    """, nativeQuery = true)
    void upsert(GithubContributionStats s);
}