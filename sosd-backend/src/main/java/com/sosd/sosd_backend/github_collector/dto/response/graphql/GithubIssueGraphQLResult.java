package com.sosd.sosd_backend.github_collector.dto.response.graphql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sosd.sosd_backend.github_collector.dto.response.GithubIssueResponseDto;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubIssueGraphQLResult(
        Search search,
        GithubRateLimit rateLimit
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Search(
            Integer issueCount,
            GithubPageInfo pageInfo,
            List<GithubIssueResponseDto> nodes   // ← 여기서 바로 사용
    ) {}
}
