package com.sosd.sosd_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record RepositoryDetailDto(
        long id,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("private") boolean _private, // 'private'는 예약어라 이름 변경
        OwnerDto owner,
        String description,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("updated_at") OffsetDateTime updatedAt,
        @JsonProperty("pushed_at") OffsetDateTime pushedAt,
        String language,
        @JsonProperty("stargazers_count") int stargazersCount,
        int forks,
        LicenseDto license,
        @JsonProperty("default_branch") String defaultBranch
) {
    public record OwnerDto(
            String login,
            long id
    ) {}

    public record LicenseDto(
            String name
    ) {}
}
