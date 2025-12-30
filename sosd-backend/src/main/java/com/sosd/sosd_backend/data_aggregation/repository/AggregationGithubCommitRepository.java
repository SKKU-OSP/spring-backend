package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.entity.github.GithubCommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AggregationGithubCommitRepository extends JpaRepository<GithubCommitEntity, Long> {

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