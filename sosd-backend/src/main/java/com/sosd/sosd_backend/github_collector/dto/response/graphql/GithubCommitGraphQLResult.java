package com.sosd.sosd_backend.github_collector.dto.response.graphql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosd.sosd_backend.github_collector.dto.response.GithubCommitResponseDto;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubCommitGraphQLResult(
        Repository repository,
        GithubRateLimit rateLimit
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Repository(DefaultBranchRef defaultBranchRef) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DefaultBranchRef(Target target) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Target(
            History history,
            @JsonProperty("oid") String headSha
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record History(
            GithubPageInfo pageInfo,
            Integer totalCount,
            List<GithubCommitResponseDto> nodes
    ) {}

    public boolean isInvalidCommits() {
        return repository == null ||
                repository.defaultBranchRef() == null ||
                repository.defaultBranchRef().target() == null ||
                repository.defaultBranchRef().target().history() == null ||
                repository.defaultBranchRef().target().history().nodes() == null ||
                repository.defaultBranchRef().target().history().nodes().isEmpty();
    }

    public List<GithubCommitResponseDto> commitsOrEmpty() {
        if (isInvalidCommits()) return List.of();
        return repository.defaultBranchRef().target().history().nodes();
    }

    public GithubPageInfo pageInfo() {
        if (repository.defaultBranchRef().target().history().pageInfo() == null) return new GithubPageInfo(false, false, null, null);
        return repository.defaultBranchRef().target().history().pageInfo();
    }

    public String headSha() {
        return repository.defaultBranchRef().target().headSha();
    }
}

