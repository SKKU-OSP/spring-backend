package com.sosd.sosd_backend.github_collector.dto.collect.context;

import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;

import java.time.OffsetDateTime;

public record StarCollectContext(
        RepoRef repoRef,
        OffsetDateTime lastStarredAt
) implements CollectContext {
}
