package com.sosd.sosd_backend.dto.github;


import com.sosd.sosd_backend.github_collector.dto.response.GithubRepositoryResponseDto;

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
        Boolean isPrivate,
        Integer openPr,
        Integer closedPr,
        Integer mergedPr,
        Integer openIssue,
        Integer closedIssue,
        Integer commit
) {
    /** GithubRepositoryResponseDto → UpsertDto 변환 */
    public static GithubRepositoryUpsertDto from(GithubRepositoryResponseDto dto) {
        return new GithubRepositoryUpsertDto(
                dto.githubRepoId(),
                dto.ownerName(),
                dto.repoName(),
                dto.defaultBranch(),
                dto.watcher(),
                dto.star(),
                dto.fork(),
                dto.dependency(),
                dto.description(),
                dto.readme(),
                dto.license(),
                toUtcLocal(dto.githubRepositoryCreatedAt()),
                toUtcLocal(dto.githubRepositoryUpdatedAt()),
                toUtcLocal(dto.githubPushedAt()),
                dto.additionalData(),
                dto.contributor(),
                dto.isPrivate(),
                dto.openPr(),
                dto.closedPr(),
                dto.mergedPr(),
                dto.openIssue(),
                dto.closedIssue(),
                dto.commit()
        );
    }

    // ---- private helpers ----
    private static LocalDateTime toUtcLocal(OffsetDateTime odt) {
        return odt != null ? odt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }

}