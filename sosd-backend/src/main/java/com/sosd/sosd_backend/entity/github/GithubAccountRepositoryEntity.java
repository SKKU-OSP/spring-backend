package com.sosd.sosd_backend.entity.github;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_account_repository")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubAccountRepositoryEntity {

    @EmbeddedId
    private GithubAccountRepositoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("githubAcountId")
    @JoinColumn(name = "github_account_id", referencedColumnName = "github_id")
    private GithubAccount githubAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("githubRepoId")      // 복합키의 repo 부분을 이 연관과 매핑
    @JoinColumn(name = "github_repo_id", referencedColumnName = "id")
    private GithubRepositoryEntity repository;

    @Column(name = "last_commit_sha", length = 40)
    private String lastCommitSha;

    @Column(name = "last_pr_date")
    private LocalDateTime lastPrDate;

    @Column(name = "last_issue_date")
    private LocalDateTime lastIssueDate;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Builder
    public GithubAccountRepositoryEntity(GithubAccount githubAccount,
                                   GithubRepositoryEntity repository,
                                   String lastCommitSha,
                                   LocalDateTime lastPrDate,
                                   LocalDateTime lastIssueDate,
                                   LocalDateTime lastUpdatedAt) {
        this.githubAccount = githubAccount;
        this.repository = repository;
        this.id = new GithubAccountRepositoryId(githubAccount.getGithubId(), repository.getId());
        this.lastCommitSha = lastCommitSha;
        this.lastPrDate = lastPrDate;
        this.lastIssueDate = lastIssueDate;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public void touchUpdatedAt() { this.lastUpdatedAt = LocalDateTime.now(); }
    public void updateCommitCursor(String sha) { this.lastCommitSha = sha; touchUpdatedAt(); }
    public void updatePrCursor(LocalDateTime dt) { this.lastPrDate = dt; touchUpdatedAt(); }
    public void updateIssueCursor(LocalDateTime dt) { this.lastIssueDate = dt; touchUpdatedAt(); }

}
