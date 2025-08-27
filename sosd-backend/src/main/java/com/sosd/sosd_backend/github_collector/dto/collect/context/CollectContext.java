package com.sosd.sosd_backend.github_collector.dto.collect.context;

public sealed interface CollectContext
    permits RepoListCollectContext, RepoDetailCollectContext, CommitCollectContext, PullRequestCollectContext, IssueCollectContext, StarCollectContext {
}
