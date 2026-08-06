package com.sosd.sosd_backend.github_collector.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** GitHub REST API GET /repos/{owner}/{repo}/commits/{sha} 응답 매핑 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubCommitDetailResponseDto(
        String sha,
        List<FileDiff> files
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FileDiff(
            String filename,
            String status,
            Integer additions,
            Integer deletions,
            @JsonProperty("patch") String patch
    ) {}
}