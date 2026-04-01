package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.entity.github.GithubIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AggregationGithubIssueRepository extends JpaRepository<GithubIssueEntity, Long> {

    // 월별 이슈 수
    // Object[]: [yymm(String), issueCount(Long)]
    @Query(value = """
        SELECT DATE_FORMAT(i.issue_date, '%Y-%m-01') AS yymm,
               COUNT(*)                              AS issue_count
        FROM github_issue i
        WHERE i.github_id = :githubId
        GROUP BY DATE_FORMAT(i.issue_date, '%Y-%m-01')
    """, nativeQuery = true)
    List<Object[]> findMonthlyIssueStatsByGithubId(@Param("githubId") Long githubId);

    @Query("""
        SELECT COUNT(i)
        FROM GithubIssueEntity i
        JOIN i.account a
        JOIN i.repository r
        WHERE a.githubId = :githubId
          AND r.id = :repoId
          AND FUNCTION('YEAR', i.issueDate) = :year
    """)
    int countByGithubIdAndRepoIdAndYear(
            @Param("githubId") Long githubId,
            @Param("repoId") Long repoId,
            @Param("year") int year);
}
