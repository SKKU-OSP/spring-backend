package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.data_aggregation.entity.GithubContributionStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AggregationGithubContributionStatsRepository extends JpaRepository<GithubContributionStats, Long> {
    Optional<GithubContributionStats> findByGithubIdAndRepoIdAndYear(Long githubId, Long repoId, int year);
}