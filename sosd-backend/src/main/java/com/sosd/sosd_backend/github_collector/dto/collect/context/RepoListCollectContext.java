package com.sosd.sosd_backend.github_collector.dto.collect.context;

import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;

import java.time.OffsetDateTime;

public record RepoListCollectContext(
        GithubAccountRef githubAccountRef,
        OffsetDateTime lastCrawling
) implements CollectContext {
}
