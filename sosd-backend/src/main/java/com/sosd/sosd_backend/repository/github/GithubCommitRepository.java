package com.sosd.sosd_backend.repository.github;

import com.sosd.sosd_backend.dto.user.RepoIdWithDate;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.github.GithubCommitEntity;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GithubCommitRepository extends JpaRepository<GithubCommitEntity, Long> {

    // repo + sha (복합 UK)로 단건 조회
    Optional<GithubCommitEntity> findByRepositoryAndSha(GithubRepositoryEntity repository, String sha);

    @Query(value = """
        SELECT c.repo_id, MAX(c.author_date) AS lastCommitDate
        FROM github_commit c
        WHERE c.github_id = :githubId
        GROUP BY c.repo_id
        ORDER BY MAX(c.author_date) DESC
        LIMIT 4
        """, nativeQuery = true)
    List<RepoIdWithDate> findRecentRepoIds(@Param("githubId") Long githubId);

    @Query(value = "SELECT COUNT(*) FROM github_commit c WHERE c.repo_id = :repoId", nativeQuery = true)
    Long countByRepoId(@Param("repoId") Long repoId);
    ////// 통계 관련 쿼리 //////
    // 특정 레포의 모든 커밋 개수
    Long countByRepository_Id(Long repositoryId);

    // 특정 계정의 모든 커밋 개수
    Long countByAccount_GithubId(Long accountGithubId);

    // 특정 년도의 모든 커밋 개수
    Long countByAuthorDateBetween(LocalDateTime start, LocalDateTime end);

    // 특정 사용자의 연도별 커밋 개수
    Long countByAccount_GithubIdAndAuthorDateBetween(
            Long accountGithubId,
            LocalDateTime start,
            LocalDateTime end);

    // 특정 사용자의 특정 레포에 대한 연도별 커밋 개수
    Long countByAccount_GithubIdAndRepository_idAndAuthorDateBetween(
            Long accountGithubId,
            Long repositoryId,
            LocalDateTime start,
            LocalDateTime end);

}
