package com.sosd.sosd_backend.repository.github;

import com.sosd.sosd_backend.dto.user.RepoGuidelineResponse;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface GithubRepositoryRepository extends JpaRepository<GithubRepositoryEntity, Long> {

    // PK로 단건 조회
    boolean existsByGithubRepoId(Long githubRepoId);
    Optional<GithubRepositoryEntity> findByGithubRepoId(Long githubRepoId);

    // githubRepoId 기준으로 벌크 로딩
    List<GithubRepositoryEntity> findAllByGithubRepoIdIn(Collection<Long> githubRepoIds);

    // 유니크 인덱스
    boolean existsByOwnerNameAndRepoName(String ownerName, String repoName);
    Optional<GithubRepositoryEntity> findByOwnerNameAndRepoName(String ownerName, String repoName);
    Optional<GithubRepositoryEntity> findByFullName(String fullName);

    // PK 다건 조회
    List<GithubRepositoryEntity> findAllByIdIn(Collection<Long> ids);

    // 변경 시각 기준 조회
    List<GithubRepositoryEntity> findAllByGithubRepositoryUpdatedAtAfter(LocalDateTime since);


    // 증분형 처리를 위한 업데이트 쿼리
    // last_starred_at 업데이트
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update GithubRepositoryEntity r set r.lastStarredAt = :ts where r.id = :repoId")
    int updateLastStarredAt(Long repoId, LocalDateTime ts);

    // last_collected_at 업데이트
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update GithubRepositoryEntity r set r.lastCollectedAt = :ts where r.id = :repoId")
    int updateLastCollectedAt(Long repoId, LocalDateTime ts);

    // 특정 유저 깃허브 전체 레포 정보 조회
    @Query(value = """
        SELECT
            r.owner_name AS ownerId,
            r.repo_name AS repoName,
            r.github_repository_created_at AS createDate,
            r.github_repository_updated_at AS updateDate,
            r.contributor AS contributorsCount,
            NULL AS releaseVer,
            0 AS releaseCount,
            CASE WHEN r.readme IS NULL THEN 0 ELSE 1 END AS readme,
            r.license AS license,
            r.description AS projShortDesc,
            r.star AS starCount,
            r.watcher AS watcherCount,
            r.fork AS forkCount,
            r.dependency AS dependencyCount,
        
            COALESCE(cs.commitCount, 0) AS commitCount,
            COALESCE(ps.prCount, 0) AS prCount,
            COALESCE(is_stats.openIssueCount, 0) AS openIssueCount,
            COALESCE(is_stats.closeIssueCount, 0) AS closeIssueCount,
        
            MAX(c.author_date) AS committerDate
        FROM github_repository r
        JOIN github_commit c\s
            ON c.repo_id = r.id AND c.github_id = :githubId
        LEFT JOIN (
            SELECT repo_id, COUNT(*) AS commitCount
            FROM github_commit
            GROUP BY repo_id
        ) cs ON cs.repo_id = r.id
        LEFT JOIN (
            SELECT repo_id, COUNT(*) AS prCount
            FROM github_pull_request
            GROUP BY repo_id
        ) ps ON ps.repo_id = r.id
        LEFT JOIN (
            SELECT repo_id,
                   SUM(CASE WHEN is_open = TRUE  THEN 1 ELSE 0 END) AS openIssueCount,
                   SUM(CASE WHEN is_open = FALSE THEN 1 ELSE 0 END) AS closeIssueCount
            FROM github_issue
            GROUP BY repo_id
        ) is_stats ON is_stats.repo_id = r.id
        GROUP BY r.id
        ORDER BY MAX(c.author_date) DESC                                                           
    """, nativeQuery = true)
    List<RepoGuidelineResponse> findRepoGuidelinesByGithubId(@Param("githubId") Long githubId);

}
