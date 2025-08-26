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
@Table(name = "github_pull_request")
public class GithubPullRequestEntity {

    // PK (AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_pr_id", nullable = false, unique = true)
    private Long githubPrId;

    @Column(name = "pr_number", nullable = false)
    private Integer prNumber;

    @Column(name = "pr_title", nullable = false, length = 255)
    private String prTitle;

    @Column(name = "pr_body", columnDefinition = "TEXT")
    private String prBody;

    @Column(name = "pr_date", nullable = false)
    private LocalDateTime prDate;

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen;

    // ===== 연관관계 =====
    // repo_id → github_repository.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id", nullable = false)
    private GithubRepositoryEntity repository;

    // github_id → github_account.github_id (작성자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "github_id", nullable = false)
    private GithubAccount account;


    @Builder
    public GithubPullRequestEntity(
            Long githubPrId,
            Integer prNumber,
            String prTitle,
            String prBody,
            LocalDateTime prDate,
            boolean isOpen,
            GithubRepositoryEntity repository,
            GithubAccount account
    ) {
        this.githubPrId = githubPrId;
        this.prNumber = prNumber;
        this.prTitle = prTitle;
        this.prBody = prBody;
        this.prDate = prDate;
        this.isOpen = isOpen;
        this.repository = repository;
        this.account = account;
    }
}

