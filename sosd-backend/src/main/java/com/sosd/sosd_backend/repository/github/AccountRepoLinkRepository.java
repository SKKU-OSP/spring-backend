package com.sosd.sosd_backend.repository.github;

import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryEntity;
import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryId;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AccountRepoLinkRepository extends JpaRepository<GithubAccountRepositoryEntity, GithubAccountRepositoryId> {

    // 계정 여러 명에 대한 레포를 한 방에 가져오기 (N+1 방지)
    @Query("""
        select gar
        from GithubAccountRepositoryEntity gar
        join fetch gar.repository r
        join gar.githubAccount a
        where a.githubId in :accountIds
    """)
    List<GithubAccountRepository> findAllByAccountIdsJoinRepo(@Param("accountIds") Collection<Long> accountIds);

    // 단일 계정의 레포만 필요할 때
    @Query("""
        select r
        from GithubAccountRepositoryEntity gar
        join gar.repository r
        where gar.githubAccount.githubId = :accountId
    """)
    List<GithubRepositoryEntity> findReposByAccountId(@Param("accountId") Long accountId);

    // 증분형 관련 처리
    // 1. lastCommitSha 업데이트
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update GithubAccountRepositoryEntity ar set ar.lastCommitSha = :sha, ar.lastUpdatedAt = :now " +
            "where ar.githubAccount.githubId = :accountId and ar.id.githubRepoId = :repoId")
    int updateLastCommitSha(Long accountId, Long repoId, String sha, LocalDateTime now);

    // 2. lastPrDate 업데이트
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update GithubAccountRepositoryEntity ar set ar.lastPrDate = :dt, ar.lastUpdatedAt = :now " +
            "where ar.githubAccount.githubId = :accountId and ar.id.githubRepoId = :repoId")
    int updateLastPrDate(Long accountId, Long repoId, LocalDateTime dt, LocalDateTime now);

    // 3. lastIssueDate 업데이트
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update GithubAccountRepositoryEntity ar set ar.lastIssueDate = :dt, ar.lastUpdatedAt = :now " +
            "where ar.githubAccount.githubId= :accountId and ar.id.githubRepoId = :repoId")
    int updateLastIssueDate(Long accountId, Long repoId, LocalDateTime dt, LocalDateTime now);

    // 4. lastUpdatedAt 단독 업데이트
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update GithubAccountRepositoryEntity ar set ar.lastUpdatedAt = :now " +
            "where ar.githubAccount.githubId = :accountId and ar.id.githubRepoId = :repoId")
    int touchLastUpdatedAt(Long accountId, Long repoId, LocalDateTime now);
}
