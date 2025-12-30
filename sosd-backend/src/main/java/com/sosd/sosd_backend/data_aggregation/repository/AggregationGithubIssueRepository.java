package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.entity.github.GithubIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AggregationGithubIssueRepository extends JpaRepository<GithubIssueEntity, Long> {

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
