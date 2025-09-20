package com.sosd.sosd_backend.entity.github;


import com.sosd.sosd_backend.dto.github.GithubRepositoryUpsertDto;
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

    @Column(name = "open_pr")
    private Integer openPr;

    @Column(name = "closed_pr")
    private Integer closedPr;

    @Column(name = "merged_pr")
    private Integer mergedPr;

    @Column(name = "open_issue")
    private Integer openIssue;

    @Column(name = "closed_issue")
    private Integer closedIssue;

    @Column(name = "`commit`")
    private Integer commit;

    @Column(name = "dependency")
    private Integer dependency;

    @Column(name = "description")
    private String description;

    @Lob
    @Column(name = "readme", columnDefinition = "MEDIUMTEXT")
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

    @Column(name = "last_starred_at")
    private LocalDateTime lastStarredAt;

    @Column(name = "last_collected_at")
    private LocalDateTime lastCollectedAt;

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


    public static GithubRepositoryEntity from(GithubRepositoryUpsertDto dto) {
        // null 체크
        if (dto.githubRepoId() == null) throw new IllegalArgumentException("Github repository id cannot be null");
        if (dto.ownerName() == null) throw new IllegalArgumentException("Owner name cannot be null");
        if (dto.repoName() == null) throw new IllegalArgumentException("Repo name cannot be null");
        if (dto.defaultBranch() == null) throw new IllegalArgumentException("Default branch cannot be null");
        if (dto.githubRepositoryUpdatedAt() == null) throw new IllegalArgumentException("Github repository updated at cannot be null");
        if (dto.githubRepositoryCreatedAt() == null) throw new IllegalArgumentException("Github repository created a cannot be null");
        if (dto.githubPushedAt() == null) throw new IllegalArgumentException("Github repository pushed at cannot be null");

        GithubRepositoryEntity e = GithubRepositoryEntity.builder()
                .githubRepoId(dto.githubRepoId())
                .ownerName(dto.ownerName())
                .repoName(dto.repoName())
                .defaultBranch(dto.defaultBranch())
                .githubRepositoryCreatedAt(dto.githubRepositoryCreatedAt())
                .githubRepositoryUpdatedAt(dto.githubRepositoryUpdatedAt())
                .githubPushedAt(dto.githubPushedAt())
                .build();
        e.merge(dto);
        return e;
    }

    /** 부분 업데이트: null/blank가 아닌 값만 반영. 식별자(githubRepoId)는 변경하지 않음. */
    public void merge(GithubRepositoryUpsertDto dto){
        if (dto.ownerName() != null) this.ownerName = dto.ownerName();
        if (dto.repoName() != null) this.repoName = dto.repoName();
        if (dto.defaultBranch() != null && !dto.defaultBranch().isBlank()) this.defaultBranch = dto.defaultBranch();
        if (dto.watcher() != null) this.watcher = dto.watcher();
        if (dto.star() != null) this.star = dto.star();
        if (dto.fork() != null) this.fork = dto.fork();
        if (dto.openPr() != null) this.openPr = dto.openPr();
        if (dto.closedPr() != null) this.closedPr = dto.closedPr();
        if (dto.mergedPr() != null) this.mergedPr = dto.mergedPr();
        if (dto.openIssue() != null) this.openIssue = dto.openIssue();
        if (dto.closedIssue() != null) this.closedIssue = dto.closedIssue();
        if (dto.commit() != null) this.commit = dto.commit();
        if (dto.dependency() != null) this.dependency = dto.dependency();
        if (dto.description() != null) this.description = dto.description();
        if (dto.readme() != null) this.readme = dto.readme();
        if (dto.license() != null) this.license = dto.license();
        if (dto.githubRepositoryCreatedAt() != null) this.githubRepositoryCreatedAt = dto.githubRepositoryCreatedAt();
        if (dto.githubRepositoryUpdatedAt() != null) this.githubRepositoryUpdatedAt = dto.githubRepositoryUpdatedAt();
        if (dto.githubPushedAt() != null) this.githubPushedAt = dto.githubPushedAt();
        if (dto.additionalData() != null) this.additionalData = dto.additionalData();
        if (dto.contributor() != null) this.contributor = dto.contributor();
        if (dto.isPrivate() != null) this.isPrivate = dto.isPrivate();
    }

}
