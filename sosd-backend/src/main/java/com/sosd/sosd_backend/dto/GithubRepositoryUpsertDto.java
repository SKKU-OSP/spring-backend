package com.sosd.sosd_backend.dto;


import com.sosd.sosd_backend.github_collector.dto.GithubRepositoryResponseDto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record GithubRepositoryUpsertDto(
        Long githubRepoId,
        String ownerName,
        String repoName,
        String defaultBranch,
        Integer watcher,
        Integer star,
        Integer fork,
        Integer dependency,
        String description,
        String readme,
        String license,
        LocalDateTime githubRepositoryCreatedAt,
        LocalDateTime githubRepositoryUpdatedAt,
        LocalDateTime githubPushedAt,
        String additionalData,
        Integer contributor,
        Boolean isPrivate
) {
    /** GithubRepositoryResponseDto → UpsertDto 변환 */
    public static GithubRepositoryUpsertDto from(GithubRepositoryResponseDto dto) {
        return new GithubRepositoryUpsertDto(
                dto.id(),
                dto.ownerNameOnly(),
                dto.repoNameOnly(),
                nvl(dto.defaultBranch(), "main"),
                dto.watchers(),
                dto.stargazersCount(),
                dto.forks(),
                null, // dependency → hydrate 단계에서
                dto.description(),
                null, // readme → hydrate 단계에서
                dto.license() != null ? dto.license().name() : null,
                toUtcLocal(dto.createdAt()),
                toUtcLocal(dto.updatedAt()),
                toUtcLocal(dto.pushedAt()),
                null, // additionalData → hydrate 단계에서
                null, // contributor → hydrate 단계에서
                dto.isPrivate()
        );
    }

    // ---- private helpers ----
    private static String nvl(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    private static LocalDateTime toUtcLocal(OffsetDateTime odt) {
        return odt != null ? odt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }

}