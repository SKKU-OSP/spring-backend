package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AggregationGithubRepositoryRepository extends JpaRepository<GithubRepositoryEntity, Long> {

    @Query("""
        SELECT r
        FROM GithubRepositoryEntity r
        WHERE r.ownerName = :owner
          AND r.githubRepositoryUpdatedAt >= (
              SELECT COALESCE(MAX(s.lastUpdatedAt), '1970-01-01')
              FROM GithubContributionStats s
              WHERE s.repoId = r.id
                AND s.githubId = :githubId
          )
    """)
    List<GithubRepositoryEntity> findReposNeedUpdate(
            @Param("githubId") Long githubId,
            @Param("owner") String owner,
            @Param("year") int year);
}
