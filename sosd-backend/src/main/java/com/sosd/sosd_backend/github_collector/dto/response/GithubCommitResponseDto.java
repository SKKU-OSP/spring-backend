package com.sosd.sosd_backend.github_collector.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubCommitResponseDto(
        @JsonProperty("oid") String sha,
        Integer additions,
        Integer deletions,
        @JsonProperty("messageHeadline") String message,
        OffsetDateTime authoredDate,
        OffsetDateTime committedDate
) {
}
