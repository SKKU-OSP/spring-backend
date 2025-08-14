package com.sosd.sosd_backend.repository.github;

import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
