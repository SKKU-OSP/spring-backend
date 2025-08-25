package com.sosd.sosd_backend.github_collector.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubStarResponseDto(
        OffsetDateTime starredAt,
        Node node
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Node(
            @JsonProperty("databaseId") Long id
    ) {}
}
