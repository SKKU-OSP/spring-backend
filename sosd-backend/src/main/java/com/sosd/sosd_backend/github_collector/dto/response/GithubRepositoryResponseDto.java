package com.sosd.sosd_backend.github_collector.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record GithubRepositoryResponseDto(
        long id,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("private") boolean isPrivate, //
        OwnerDto owner,
        String description,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("updated_at") OffsetDateTime updatedAt,
        @JsonProperty("pushed_at") OffsetDateTime pushedAt,
        String language,
        @JsonProperty("stargazers_count") int stargazersCount,
        int forks,
        LicenseDto license,
        @JsonProperty("default_branch") String defaultBranch,
        int watchers
) {
    public record OwnerDto(
            String login,
            long id
    ) {}

    public record LicenseDto(
            String name
    ) {}

    /** fullName에서 repoName 부분만 추출 */
    public String repoNameOnly() {
        if (fullName == null) return null;
        int idx = fullName.indexOf('/');
        return (idx >= 0 && idx + 1 < fullName.length()) ? fullName.substring(idx + 1) : fullName;
    }

    /** fullName에서 ownerName 부분만 추출 */
    public String ownerNameOnly() {
        if (fullName == null) return null;
        int idx = fullName.indexOf('/');
        return (idx > 0) ? fullName.substring(0, idx) : null;
    }
}
