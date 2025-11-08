package com.sosd.sosd_backend.data_aggregation.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubPullRequestRepository {

    @Query("""
        SELECT COUNT(p) FROM GithubPullRequestEntity p
        WHERE p.account = :githubId
          AND p.repository = :repoId
          AND FUNCTION('YEAR', p.prDate) = :year
    """)
    int countByGithubIdAndRepoIdAndYear(
            @Param("githubId") Long githubId,
            @Param("repoId") Long repoId,
            @Param("year") int year);
}