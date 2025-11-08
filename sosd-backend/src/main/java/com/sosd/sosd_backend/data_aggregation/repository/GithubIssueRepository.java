package com.sosd.sosd_backend.data_aggregation.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubIssueRepository {

    @Query("""
        SELECT COUNT(i) FROM GithubIssueEntity i
        WHERE i.account = :githubId
          AND i.repository = :repoId
          AND FUNCTION('YEAR', i.issueDate) = :year
    """)
    int countByGithubIdAndRepoIdAndYear(
            @Param("githubId") Long githubId,
            @Param("repoId") Long repoId,
            @Param("year") int year);
}
