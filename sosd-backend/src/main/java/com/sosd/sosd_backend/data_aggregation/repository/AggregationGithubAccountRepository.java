package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.entity.github.GithubAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AggregationGithubAccountRepository extends JpaRepository<GithubAccount, Long> {

    @Query("""
        SELECT a FROM GithubAccount a
        WHERE a.lastCrawling > (
            SELECT COALESCE(MAX(s.lastUpdatedAt), '1970-01-01')
            FROM GithubContributionStats s
            WHERE s.githubId = a.githubId
        )
    """)
    List<GithubAccount> findAccountsNeedUpdate(@Param("year") int year);
}