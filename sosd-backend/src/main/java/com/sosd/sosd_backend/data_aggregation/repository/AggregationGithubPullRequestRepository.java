package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.entity.github.GithubPullRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AggregationGithubPullRequestRepository extends JpaRepository<GithubPullRequestEntity, Long> {

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