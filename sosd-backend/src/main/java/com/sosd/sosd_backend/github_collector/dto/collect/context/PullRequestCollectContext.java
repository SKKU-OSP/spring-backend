package com.sosd.sosd_backend.github_collector.dto.collect.context;

import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;

import java.time.OffsetDateTime;

public record PullRequestCollectContext (
        GithubAccountRef githubAccountRef,
        RepoRef repoRef,
        OffsetDateTime lastPrDate
) implements CollectContext  {
}
