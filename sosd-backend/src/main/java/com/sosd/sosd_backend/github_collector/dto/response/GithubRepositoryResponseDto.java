package com.sosd.sosd_backend.github_collector.dto.response;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.sosd_backend.github_collector.dto.response.graphql.GithubRepositoryGraphQLResult;

/** RepoOverview → 평탄화 DTO (lastStarredAt/lastCollectedAt 제외) */
public record GithubRepositoryResponseDto(
        Long githubRepoId,
        String ownerName,
        String repoName,
        String fullName,
        String defaultBranch,
        Integer watcher,
        Integer star,
        Integer fork,
        Integer dependency,
        String description,
        String readme,
        String license,
        OffsetDateTime githubRepositoryCreatedAt,
        OffsetDateTime githubRepositoryUpdatedAt,
        OffsetDateTime githubPushedAt,
        String additionalData,
        Integer contributor,
        Boolean isPrivate
) {
    /** GraphQL 응답 → 평탄화 DTO */
    public static GithubRepositoryResponseDto from(GithubRepositoryGraphQLResult gql) {
        var repo = Objects.requireNonNull(gql.repository(), "repository is null");

        Long repoId = repo.databaseId() != null ? repo.databaseId().longValue() : null;
        String nameWithOwner = repo.nameWithOwner();
        String owner = splitOwner(nameWithOwner);
        String name  = splitRepo(nameWithOwner);

        String defaultBranch = repo.defaultBranchRef() != null
                ? repo.defaultBranchRef().name() : "main";

        Integer watchers   = repo.watchers() != null ? repo.watchers().totalCount() : null;
        Integer dependency = repo.dependencyGraphManifests() != null ? repo.dependencyGraphManifests().totalCount() : null;
        String readme      = repo.object() != null ? repo.object().text() : null;
        String license     = repo.licenseInfo() != null ? repo.licenseInfo().name() : null;

        String additionalJson = buildLanguagesJson(repo.languages());
        Integer contributor = repo.mentionableUsers() != null ? repo.mentionableUsers().totalCount() : null;

        return new GithubRepositoryResponseDto(
                repoId,
                owner,
                name,
                nameWithOwner,
                defaultBranch,
                watchers,
                repo.stargazerCount(),
                repo.forkCount(),
                dependency,
                repo.description(),
                readme,
                license,
                repo.createdAt(),
                repo.updatedAt(),
                repo.pushedAt(),
                additionalJson,
                contributor,
                repo.isPrivate()
        );
    }

    // ===== helpers =====
    private static String splitOwner(String nameWithOwner) {
        if (nameWithOwner == null) return null;
        int idx = nameWithOwner.indexOf('/');
        return idx > 0 ? nameWithOwner.substring(0, idx) : null;
    }
    private static String splitRepo(String nameWithOwner) {
        if (nameWithOwner == null) return null;
        int idx = nameWithOwner.indexOf('/');
        return (idx >= 0 && idx < nameWithOwner.length() - 1) ? nameWithOwner.substring(idx + 1) : null;
    }

    /** languages → JSON: { "totalSize":..., "languages":[{"name":"Java","size":123}, ...] } */
    private static String buildLanguagesJson(GithubRepositoryGraphQLResult.Languages languages) {
        if (languages == null) return null;

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("totalSize", languages.totalSize());

        List<Map<String, Object>> list = Optional.ofNullable(languages.edges())
                .orElseGet(List::of)
                .stream()
                .map(edge -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", edge != null && edge.node() != null ? edge.node().name() : null);
                    m.put("size", edge != null ? edge.size() : null);
                    return m;
                })
                .collect(Collectors.toList());

        root.put("languages", list);

        try {
            return new ObjectMapper().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"failed to serialize languages\"}";
        }
    }
}
