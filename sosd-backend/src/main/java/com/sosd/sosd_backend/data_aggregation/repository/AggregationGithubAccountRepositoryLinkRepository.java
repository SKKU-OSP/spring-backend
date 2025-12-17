package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.data_aggregation.dto.AccountRepoProjection;
import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryEntity;
import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AggregationGithubAccountRepositoryLinkRepository extends JpaRepository<GithubAccountRepositoryEntity, GithubAccountRepositoryId> {

    @Query("""
        SELECT new com.sosd.sosd_backend.data_aggregation.dto.AccountRepoProjection(
            gar.githubAccount.githubId,
            gar.repository.id,
            gar.repository.readme,
            gar.repository.license,
            gar.repository.description,
            gar.repository.star,
            gar.repository.fork,
            gar.lastUpdatedAt
        )
        FROM GithubAccountRepositoryEntity gar
        WHERE NOT EXISTS (
            SELECT 1 FROM GithubContributionStats s
            WHERE s.githubId = gar.githubAccount.githubId
              AND s.repoId = gar.repository.id
              AND s.lastUpdatedAt >= gar.lastUpdatedAt
        )
    """)
    List<AccountRepoProjection> findLinksNeedUpdate();
}