package com.sosd.sosd_backend.github_collector.dto.response.graphql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sosd.sosd_backend.github_collector.dto.response.GithubPullRequestResponseDto;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubPullRequestGraphQLResult(
        Search search,
        GithubRateLimit rateLimit
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Search(
            Integer issueCount,
            GithubPageInfo pageInfo,
            List<GithubPullRequestResponseDto> nodes   // ← 여기서 바로 사용
    ) {}

}
