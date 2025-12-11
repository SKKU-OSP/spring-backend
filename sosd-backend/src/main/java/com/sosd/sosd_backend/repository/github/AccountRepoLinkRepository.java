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
import java.util.Optional;

public interface AccountRepoLinkRepository extends JpaRepository<GithubAccountRepositoryEntity, GithubAccountRepositoryId> {

    // 여러 계정의 링크를 한번에, 레포까지 fetch (중복 방지: distinct)
    @Query("""
        select distinct gar
        from GithubAccountRepositoryEntity gar
        join fetch gar.repository r
        where gar.id.githubAccountId in :accountIds
    """)
    List<GithubAccountRepositoryEntity> findAllByAccountIdsJoinRepo(@Param("accountIds") Collection<Long> accountIds);

    // 단일 계정의 레포 목록만
    @Query("""
        select r
        from GithubAccountRepositoryEntity gar
        join gar.repository r
        where gar.id.githubAccountId = :accountId
    """)
    List<GithubRepositoryEntity> findReposByAccountId(@Param("accountId") Long accountId);

    // === 커서 단건 조회 ===
    @Query("""
    select gar.lastCommitSha
    from GithubAccountRepositoryEntity gar
    where gar.id.githubAccountId = :accountId
      and gar.id.githubRepoId    = :repoId
""")
    Optional<String> findLastCommitSha(@Param("accountId") Long accountId,
                                       @Param("repoId") Long repoId);

    @Query("""
    select gar.lastPrDate
    from GithubAccountRepositoryEntity gar
    where gar.id.githubAccountId = :accountId
      and gar.id.githubRepoId    = :repoId
""")
    Optional<LocalDateTime> findLastPrDate(@Param("accountId") Long accountId,
                                           @Param("repoId") Long repoId);

    @Query("""
    select gar.lastIssueDate
    from GithubAccountRepositoryEntity gar
    where gar.id.githubAccountId = :accountId
      and gar.id.githubRepoId    = :repoId
""")
    Optional<LocalDateTime> findLastIssueDate(@Param("accountId") Long accountId,
                                              @Param("repoId") Long repoId);

    @Query("""
    select gar.lastUpdatedAt
    from GithubAccountRepositoryEntity gar
    where gar.id.githubAccountId = :accountId
        and gar.id.githubRepoId    = :repoId
""")
    Optional<LocalDateTime> findLastUpdatedAt(@Param("accountId") Long accountId,
                                              @Param("repoId") Long repoId);


    // === 커서/업데이트 계열 (DB 시간 사용) ===
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update GithubAccountRepositoryEntity ar
        set ar.lastCommitSha = :sha
        where ar.id.githubAccountId = :accountId
          and ar.id.githubRepoId    = :repoId
    """)
    int updateLastCommitSha(@Param("accountId") Long accountId,
                            @Param("repoId") Long repoId,
                            @Param("sha") String sha);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update GithubAccountRepositoryEntity ar
        set ar.lastPrDate = :dt
        where ar.id.githubAccountId = :accountId
          and ar.id.githubRepoId    = :repoId
    """)
    int updateLastPrDate(@Param("accountId") Long accountId,
                         @Param("repoId") Long repoId,
                         @Param("dt") LocalDateTime dt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update GithubAccountRepositoryEntity ar
        set ar.lastIssueDate = :dt
        where ar.id.githubAccountId = :accountId
          and ar.id.githubRepoId    = :repoId
    """)
    int updateLastIssueDate(@Param("accountId") Long accountId,
                            @Param("repoId") Long repoId,
                            @Param("dt") LocalDateTime dt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update GithubAccountRepositoryEntity ar
    set ar.lastUpdatedAt = :now
    where ar.id.githubAccountId = :accountId
      and ar.id.githubRepoId    = :repoId
""")
    int touchLastUpdatedAt(@Param("accountId") Long accountId,
                           @Param("repoId") Long repoId,
                           @Param("now") LocalDateTime now);


}
