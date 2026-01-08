package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.data_aggregation.entity.GithubContributionStats;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Query("""
        SELECT new com.sosd.sosd_backend.service.ContributionAggregate(
            COALESCE(SUM(s.commitCount), 0),
            COALESCE(SUM(s.commitLines), 0),
            COALESCE(SUM(s.prCount), 0),
            COALESCE(SUM(s.issueCount), 0),
            COALESCE(SUM(s.starCount), 0),
            COALESCE(SUM(s.forkCount), 0),
            COALESCE(MAX(s.guidelineScore), 0.0)
        )
        FROM GithubContributionStats s
        WHERE s.githubId = :githubId
          AND s.year = :year
    """)
    Optional<com.sosd.sosd_backend.service.ContributionAggregate> aggregateByGithubIdAndYear(Long githubId, int year);

    @Query("""
        SELECT r.fullName
        FROM GithubContributionStats s
        JOIN GithubRepositoryEntity r ON s.repoId = r.id
        WHERE s.githubId = :githubId
          AND s.year = :year
        ORDER BY s.commitLines DESC
    """)
    List<String> findTopRepoFullNameByCommitLines(Long githubId, int year);

    /**
     * 특정 사용자의 연도별 레포별 통계 조회 (레포 점수 내림차순)
     */
    @Query("""
        SELECT s
        FROM GithubContributionStats s
        WHERE s.githubId = :githubId
          AND s.year = :year
        ORDER BY s.repoScore DESC
    """)
    List<GithubContributionStats> findAllByGithubIdAndYearOrderByRepoScoreDesc(Long githubId, int year);

    /**
     * 특정 사용자의 연도별 레포별 통계 조회 (레포 이름 포함)
     */
    @Query("""
        SELECT s, r.fullName
        FROM GithubContributionStats s
        JOIN GithubRepositoryEntity r ON s.repoId = r.id
        WHERE s.githubId = :githubId
          AND s.year = :year
        ORDER BY s.repoScore DESC
    """)
    List<Object[]> findAllWithRepoNameByGithubIdAndYear(Long githubId, int year);
}
