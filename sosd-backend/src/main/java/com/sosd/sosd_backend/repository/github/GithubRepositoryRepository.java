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
    boolean existsByRepoId(Long repoId);
    Optional<GithubRepositoryEntity> findByRepoId(Long repoId);

    // 유니크 인덱스
    boolean existsByOwnerNameAndRepoName(String ownerName, String repoName);
    Optional<GithubRepositoryEntity> findByOwnerNameAndRepoName(String ownerName, String repoName);

    // GithubAccount로 일괄 조회
    List<GithubRepositoryEntity> findAllByGithubAccount(GithubAccount githubAccount);

    // GithubAccount의 PK(=github_id)로 조회
    List<GithubRepositoryEntity> findAllByGithubAccount_GithubId(Integer githubId);

    List<GithubRepositoryEntity> findAllByGithubAccount_GithubLoginUsername(String githubLoginUsername);

    // 여러 repoId로 한 번에 조회
    List<GithubRepositoryEntity> findAllByRepoIdIn(Collection<Integer> repoIds);

}
