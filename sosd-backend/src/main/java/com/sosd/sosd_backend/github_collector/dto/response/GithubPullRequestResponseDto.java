package com.sosd.sosd_backend.github_collector.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubPullRequestResponseDto(
        Long databaseId,
        Integer number,
        String title,
        String body,
        String state,             // OPEN / CLOSED / MERGED
        OffsetDateTime createdAt
) {}
