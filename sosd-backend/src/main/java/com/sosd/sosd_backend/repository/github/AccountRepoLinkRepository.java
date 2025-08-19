package com.sosd.sosd_backend.repository.github;

import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryEntity;
import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryId;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
