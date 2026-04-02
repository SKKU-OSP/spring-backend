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
        OffsetDateTime committedDate,
        Author author
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Author(
            String name,
            String email,
            User user
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(
            String login
    ) {}

    public String authorLogin() {
        if (author == null || author.user() == null) return null;
        return author.user().login();
    }

    public String authorEmail() {
        if (author == null) return null;
        return author.email();
    }
}
