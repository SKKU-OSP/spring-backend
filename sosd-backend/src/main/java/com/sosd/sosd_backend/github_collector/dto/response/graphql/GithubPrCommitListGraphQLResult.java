package com.sosd.sosd_backend.github_collector.dto.response.graphql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubPrCommitListGraphQLResult(
        Repository repository,
        GithubRateLimit rateLimit
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Repository(PullRequest pullRequest) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PullRequest(CommitConnection commits) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CommitConnection(
            int totalCount,
            GithubPageInfo pageInfo,
            List<PullRequestCommitNode> nodes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PullRequestCommitNode(Commit commit) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Commit(
            String oid,
            String messageHeadline,
            Integer changedFilesIfAvailable,
            int additions,
            int deletions
    ) {}
}
