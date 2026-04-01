package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.entity.github.GithubPullRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AggregationGithubPullRequestRepository extends JpaRepository<GithubPullRequestEntity, Long> {

    // 월별 PR 수
    // Object[]: [yymm(String), prCount(Long)]
    @Query(value = """
        SELECT DATE_FORMAT(p.pr_date, '%Y-%m-01') AS yymm,
               COUNT(*)                           AS pr_count
        FROM github_pull_request p
        WHERE p.github_id = :githubId
        GROUP BY DATE_FORMAT(p.pr_date, '%Y-%m-01')
    """, nativeQuery = true)
    List<Object[]> findMonthlyPrStatsByGithubId(@Param("githubId") Long githubId);

    @Query("""
        SELECT COUNT(p)
        FROM GithubPullRequestEntity p
        JOIN p.account a
        JOIN p.repository r
        WHERE a.githubId = :githubId
          AND r.id = :repoId
          AND FUNCTION('YEAR', p.prDate) = :year
    """)
    int countByGithubIdAndRepoIdAndYear(
            @Param("githubId") Long githubId,
            @Param("repoId") Long repoId,
            @Param("year") int year);
}