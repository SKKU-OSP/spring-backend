package com.sosd.sosd_backend.github_collector.dto.collect.context;

import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;

import java.time.OffsetDateTime;

public record IssueCollectContext(
        GithubAccountRef githubAccountRef,
        RepoRef repoRef,
        OffsetDateTime lastIssueDate
) implements CollectContext {
}
