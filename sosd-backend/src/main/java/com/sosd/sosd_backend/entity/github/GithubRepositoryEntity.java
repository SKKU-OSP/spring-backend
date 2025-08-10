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
@Table(name = "repository")
public class GithubRepositoryEntity {
    @Id
    @Column(name = "repo_id", nullable = false)
    private Long repoId;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "repo_name", nullable = false)
    private String repoName;

    @Column(name = "default_branch", nullable = false)
    private String defaultBranch;

    @Column(name = "score")
    private Integer score;

    @Column(name = "watcher")
    private Integer watcher;

    @Column(name = "star")
    private Integer star;

    @Column(name = "fork")
    private Integer fork;

    @Column(name = "commit_count")
    private Integer commitCount;

    @Column(name = "commit_line")
    private Integer commitLine;

    @Column(name = "commit_del")
    private Integer commitDel;

    @Column(name = "commit_add")
    private Integer commitAdd;

    @Column(name = "unmerged_commit_count")
    private Integer unmergedCommitCount;

    @Column(name = "unmerged_commit_line")
    private Integer unmergedCommitLine;

    @Column(name = "unmerged_commit_del")
    private Integer unmergedCommitDel;

    @Column(name = "unmerged_commit_add")
    private Integer unmergedCommitAdd;

    @Column(name = "pr")
    private Integer pr;

    @Column(name = "issue")
    private Integer issue;

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

    @Column(name = "pushed_at", nullable = false)
    private LocalDateTime pushedAt;

    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;

    @Column(name = "contributor")
    private Integer contributor;

    @Column(name = "is_private")
    private Boolean isPrivate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "github_id", nullable = false)
    private GithubAccount githubAccount;

    @Builder
    public GithubRepositoryEntity(
            Long repoId,
            String ownerName,
            String repoName,
            String defaultBranch,
            LocalDateTime githubRepositoryCreatedAt,
            LocalDateTime githubRepositoryUpdatedAt,
            LocalDateTime pushedAt,
            GithubAccount githubAccount
    ) {
        this.repoId = repoId;
        this.ownerName = ownerName;
        this.repoName = repoName;
        this.defaultBranch = defaultBranch;
        this.githubRepositoryCreatedAt = githubRepositoryCreatedAt;
        this.githubRepositoryUpdatedAt = githubRepositoryUpdatedAt;
        this.pushedAt = pushedAt;
        this.githubAccount = githubAccount;
    }

}
