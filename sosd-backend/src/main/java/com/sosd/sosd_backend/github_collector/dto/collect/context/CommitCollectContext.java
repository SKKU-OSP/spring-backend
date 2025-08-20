package com.sosd.sosd_backend.github_collector.dto.collect.context;

import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;

public record CommitCollectContext(
        GithubAccountRef githubAccountRef,
        RepoRef repoRef,
        String lastCommitSha
) {
}
