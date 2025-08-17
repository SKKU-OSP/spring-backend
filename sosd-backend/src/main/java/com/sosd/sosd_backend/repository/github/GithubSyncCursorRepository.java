package com.sosd.sosd_backend.repository.github;
import com.sosd.sosd_backend.entity.github.GithubSyncCursor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GithubSyncCursorRepository extends JpaRepository<GithubSyncCursor, GithubSyncCursor.CursorId> {

    // 계정-레포의 모든 커서
    List<GithubSyncCursor> findByIdGithubAccountIdAndIdGithubRepoId(Long accountId, Long repoId);

    // 계정-레포-타입 단건 조회
    Optional<GithubSyncCursor> findByIdGithubAccountIdAndIdGithubRepoIdAndIdResourceType(
            Long accountId, Long repoId, GithubSyncCursor.ResourceType type);
}
