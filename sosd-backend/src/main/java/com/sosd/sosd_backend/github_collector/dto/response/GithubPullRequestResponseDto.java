package com.sosd.sosd_backend.github_collector.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;

/**
 * (GraphQL 응답에서 필요한 부분만 매핑)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubPullRequestResponseDto(
        String id,
        Integer number,
        String title,
        String body,
        String state,             // OPEN / CLOSED / MERGED
        OffsetDateTime createdAt
) {}
