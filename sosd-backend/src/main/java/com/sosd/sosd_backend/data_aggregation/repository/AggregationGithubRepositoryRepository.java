package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AggregationGithubRepositoryRepository extends JpaRepository<GithubRepositoryEntity, Long> {

    // 월별 생성 레포 수 (owner_name = 이 유저의 login username)
    // Object[]: [yymm(String), crRepos(Long)]
    @Query(value = """
        SELECT DATE_FORMAT(r.github_repository_created_at, '%Y-%m-01') AS yymm,
               COUNT(*)                                                 AS cr_repos
        FROM github_repository r
        WHERE r.owner_name = :loginUsername
        GROUP BY DATE_FORMAT(r.github_repository_created_at, '%Y-%m-01')
    """, nativeQuery = true)
    List<Object[]> findMonthlyCrReposByLoginUsername(@Param("loginUsername") String loginUsername);

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
