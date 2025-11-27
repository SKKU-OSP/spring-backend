package com.sosd.sosd_backend.github_collector.dto.ref;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 깃허브 수집 로직에서 사용할 github 계정 잠조 정보 DTO
 *
 * @param githubId
 * @param githubGraphqlNodeId
 * @param githubLoginUsername
 * @param githubName
 * @param githubToken
 * @param githubEmail
 */
public record GithubAccountRef(
        Long githubId,
        String githubGraphqlNodeId,
        String githubLoginUsername,
        String githubName,
        String githubToken,
        String githubEmail,
        LocalDateTime lastCrawling
        ) {
    public GithubAccountRef{
        Objects.requireNonNull(githubId);
        Objects.requireNonNull(githubGraphqlNodeId);
        Objects.requireNonNull(githubLoginUsername);
    }
}
