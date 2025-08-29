package com.sosd.sosd_backend.github_collector.dto.response.graphql;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * GraphQL RepoOverview 쿼리 응답 매핑용 DTO
 */
public record GithubRepositoryGraphQLResult(
        Repository repository,
        GithubRateLimit rateLimit
) {
    public record Repository(
            Integer databaseId,
            String nameWithOwner,
            Boolean isPrivate,
            DefaultBranchRef defaultBranchRef,
            String description,
            Integer stargazerCount,
            Watchers watchers,
            Integer forkCount,
            LicenseInfo licenseInfo,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime pushedAt,
            ReadmeObject object,
            Languages languages,
            DependencyGraphManifests dependencyGraphManifests,
            MentionableUsers mentionableUsers
    ) {}

    public record DefaultBranchRef(
            String name
    ) {}

    public record Watchers(
            Integer totalCount
    ) {}

    public record LicenseInfo(
            String name
    ) {}

    public record ReadmeObject(
            String text
    ) {}

    public record Languages(
            Integer totalSize,
            List<LanguageEdge> edges
    ) {}

    public record LanguageEdge(
            Integer size,
            LanguageNode node
    ) {}

    public record LanguageNode(
            String name
    ) {}

    public record DependencyGraphManifests(
            Integer totalCount
    ) {}
    public record MentionableUsers(
            Integer totalCount
    ) {}
}
