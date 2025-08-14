package com.sosd.sosd_backend.github_collector.dto;

import java.util.Objects;

/**
 * 후속 수집기 호출에 필요한 최소한의 레포 참조 정보 DTO.
 *
 * @param repoId       내부 PK (auto increment)
 * @param githubRepoId GitHub 내부 고유 ID
 * @param ownerName    저장소 소유자 (user/org)
 * @param repoName     저장소 이름
 * @param fullName     ownerName/repoName
 */
public record RepoRef(
        Long repoId,
        Long githubRepoId,
        String ownerName,
        String repoName,
        String fullName
) {
    public RepoRef {
        Objects.requireNonNull(repoId, "repoId must not be null");
        Objects.requireNonNull(githubRepoId, "githubRepoId must not be null");
        Objects.requireNonNull(ownerName, "ownerName must not be null");
        Objects.requireNonNull(repoName, "repoName must not be null");
        Objects.requireNonNull(fullName, "fullName must not be null");
    }
}
