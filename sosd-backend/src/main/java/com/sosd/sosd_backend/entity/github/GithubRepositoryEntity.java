package com.sosd.sosd_backend.entity.github;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "github_repository")
public class GithubRepositoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "github_repo_id", nullable = false)
    private Long githubRepoId;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "repo_name", nullable = false)
    private String repoName;

    @Column(name = "full_name", insertable = false, updatable = false)
    private String fullName;

    @Column(name = "default_branch", nullable = false)
    private String defaultBranch;

    @Column(name = "watcher")
    private Integer watcher;

    @Column(name = "star")
    private Integer star;

    @Column(name = "fork")
    private Integer fork;

    @Column(name = "dependency")
    private Integer dependency;

    @Column(name = "description")
    private String description;

    @Column(name = "readme", columnDefinition = "TEXT")
    private String readme;

    @Column(name = "license")
    private String license;

    @Column(name = "github_repository_created_at", nullable = false)
    private LocalDateTime githubRepositoryCreatedAt;

    @Column(name = "github_repository_updated_at", nullable = false)
    private LocalDateTime githubRepositoryUpdatedAt;

    @Column(name = "github_pushed_at", nullable = false)
    private LocalDateTime githubPushedAt;

    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;

    @Column(name = "contributor")
    private Integer contributor;

    @Column(name = "is_private")
    private Boolean isPrivate;

    @Builder
    public GithubRepositoryEntity(
            Long githubRepoId,
            String ownerName,
            String repoName,
            String defaultBranch,
            LocalDateTime githubRepositoryCreatedAt,
            LocalDateTime githubRepositoryUpdatedAt,
            LocalDateTime githubPushedAt
    ) {
        this.githubRepoId = githubRepoId;
        this.ownerName = ownerName;
        this.repoName = repoName;
        this.defaultBranch = defaultBranch;
        this.githubRepositoryCreatedAt = githubRepositoryCreatedAt;
        this.githubRepositoryUpdatedAt = githubRepositoryUpdatedAt;
        this.githubPushedAt = githubPushedAt;
    }

}
