package com.sosd.sosd_backend.github_collector.dto.collect.context;

import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;

import java.time.OffsetDateTime;

public record RepoDetailCollectContext(
        RepoRef repoRef,
        OffsetDateTime lastCollectedAt
) {
}
