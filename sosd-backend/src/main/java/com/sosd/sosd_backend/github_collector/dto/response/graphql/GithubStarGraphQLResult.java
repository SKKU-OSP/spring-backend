package com.sosd.sosd_backend.github_collector.dto.response.graphql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sosd.sosd_backend.github_collector.dto.response.GithubStarResponseDto;

import java.util.List;

public record GithubStarGraphQLResult(
        Repository repository
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Repository(Stargazers stargazers) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Stargazers(
            GithubPageInfo pageInfo,
            List<GithubStarResponseDto> edges
    ) {}

    // 숏컷 유틸 함수
    public boolean isInvalidStargazer() {
        return this.repository == null || this.repository.stargazers == null;
    }

    public List<GithubStarResponseDto> getStars() {
        if (this.isInvalidStargazer()) return List.of();
        return this.repository.stargazers.edges;
    }

    public GithubPageInfo getPageInfo() {
        if (this.isInvalidStargazer()) return new GithubPageInfo(false, false, null, null);
        return this.repository.stargazers.pageInfo;
    }
}
