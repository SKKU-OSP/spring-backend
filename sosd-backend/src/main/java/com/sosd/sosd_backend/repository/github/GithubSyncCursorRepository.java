package com.sosd.sosd_backend.repository.github;

import com.sosd.sosd_backend.entity.github.GithubSyncCursor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GithubSyncCursorRepository extends JpaRepository<GithubSyncCursor, GithubSyncCursor.CursorId> {

    // 복합키로 조회
    default Optional<GithubSyncCursor> findByCursor(Long accountId, Long repoId, GithubSyncCursor.ResourceType type) {
        return findById(new GithubSyncCursor.CursorId(accountId, repoId, type));
    }

    // 계정-레포의 모든 커서
    List<GithubSyncCursor> findByGithubAccountIdAndGithubRepoId(Long accountId, Long repoId);

}
