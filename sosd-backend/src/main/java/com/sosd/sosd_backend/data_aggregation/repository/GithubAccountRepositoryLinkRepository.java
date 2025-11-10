package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.data_aggregation.dto.AccountRepoPair;
import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryEntity;
import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GithubAccountRepositoryLinkRepository extends JpaRepository<GithubAccountRepositoryEntity, GithubAccountRepositoryId> {

    @Query("""
    SELECT gar
    FROM GithubAccountRepositoryEntity gar
    WHERE NOT EXISTS (
        SELECT 1
        FROM GithubContributionStats s
        WHERE s.githubId = gar.githubAccount.githubId
          AND s.repoId = gar.repository.id
          AND s.lastUpdatedAt >= gar.lastUpdatedAt
    )
    """)
    List<GithubAccountRepositoryEntity> findLinksNeedUpdate();
}
