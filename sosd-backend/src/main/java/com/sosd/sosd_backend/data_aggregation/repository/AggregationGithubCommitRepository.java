package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.entity.github.GithubCommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AggregationGithubCommitRepository extends JpaRepository<GithubCommitEntity, Long> {

    // 월별 커밋 수 + 기여 레포 수 (co_repos)
    // Object[]: [yymm(String), commitCount(Long), coRepos(Long)]
    @Query(value = """
        SELECT DATE_FORMAT(c.author_date, '%Y-%m-01') AS yymm,
               COUNT(*)                               AS commit_count,
               COUNT(DISTINCT c.repo_id)              AS co_repos
        FROM github_commit c
        WHERE c.github_id = :githubId
        GROUP BY DATE_FORMAT(c.author_date, '%Y-%m-01')
    """, nativeQuery = true)
    List<Object[]> findMonthlyCommitStatsByGithubId(@Param("githubId") Long githubId);

    @Query("""
        SELECT COUNT(c)
        FROM GithubCommitEntity c
        JOIN c.account a
        JOIN c.repository r
        WHERE a.githubId = :githubId
          AND r.id = :repoId
          AND FUNCTION('YEAR', c.authorDate) = :year
    """)
    int countByGithubIdAndRepoIdAndYear(
            @Param("githubId") Long githubId,
            @Param("repoId") Long repoId,
            @Param("year") int year);

    @Query("""
        SELECT COALESCE(SUM(c.addition + c.deletion), 0)
        FROM GithubCommitEntity c
        JOIN c.account a
        JOIN c.repository r
        WHERE a.githubId = :githubId
          AND r.id = :repoId
          AND FUNCTION('YEAR', c.authorDate) = :year
    """)
    int sumLinesByGithubIdAndRepoIdAndYear(
            @Param("githubId") Long githubId,
            @Param("repoId") Long repoId,
            @Param("year") int year);
}