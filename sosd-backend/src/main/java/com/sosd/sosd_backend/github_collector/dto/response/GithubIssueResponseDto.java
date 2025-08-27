package com.sosd.sosd_backend.github_collector.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubIssueResponseDto(
        String id,
        Integer number,
        String title,
        String body,
        OffsetDateTime createdAt
) {
}
