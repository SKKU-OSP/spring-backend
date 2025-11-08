package com.sosd.sosd_backend.data_aggregation.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubCommitRepository {

    @Query("""
        SELECT COUNT(c) FROM GithubCommitEntity c
        WHERE c.account = :githubId
          AND c.repository = :repoId
          AND FUNCTION('YEAR', c.authorDate) = :year
    """)
    int countByGithubIdAndRepoIdAndYear(
            @Param("githubId") Long githubId,
            @Param("repoId") Long repoId,
            @Param("year") int year);

    @Query("""
        SELECT COALESCE(SUM(c.addition + c.deletion), 0)
        FROM GithubCommitEntity c
        WHERE c.account = :githubId
          AND c.repository = :repoId
          AND FUNCTION('YEAR', c.authorDate) = :year
    """)
    int sumLinesByGithubIdAndRepoIdAndYear(
            @Param("githubId") Long githubId,
            @Param("repoId") Long repoId,
            @Param("year") int year);
}
