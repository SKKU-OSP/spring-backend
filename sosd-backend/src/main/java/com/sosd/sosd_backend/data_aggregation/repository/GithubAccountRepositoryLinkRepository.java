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
        SELECT new com.sosd.sosd_backend.data_aggregation.dto.AccountRepoPair(
            ar.githubAccountId,
            ar.githubRepoId,
            ar.lastUpdatedAt
        )
        FROM GithubAccountRepositoryEntity ar
        WHERE ar.lastUpdatedAt > (
            SELECT COALESCE(MAX(s.lastUpdatedAt), '1970-01-01')
            FROM GithubContributionStats s
            WHERE s.githubId = ar.githubAccountId
              AND s.repoId = ar.githubRepoId
        )
    """)
    List<AccountRepoPair> findPairsNeedUpdate();
}
