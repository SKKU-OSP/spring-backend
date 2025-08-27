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
@Table(name = "github_issue")
public class GithubIssueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "github_issue_id", nullable = false)
    private Long githubIssueId; // GitHub 제공 고유 Issue ID

    @Column(name = "issue_number", nullable = false)
    private Integer issueNumber; // 저장소 내 고유 번호 (#123 등)

    @Column(name = "issue_title", nullable = false, length = 255)
    private String issueTitle;

    @Column(name = "issue_body", columnDefinition = "TEXT")
    private String issueBody;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    // ===== 연관관계 =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id", nullable = false)
    private GithubRepositoryEntity repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "github_id", nullable = false)
    private GithubAccount account;

    @Builder
    public GithubIssueEntity(
            Long githubIssueId,
            Integer issueNumber,
            String issueTitle,
            String issueBody,
            LocalDateTime issueDate,
            GithubRepositoryEntity repository,
            GithubAccount account
    ) {
        this.githubIssueId = githubIssueId;
        this.issueNumber = issueNumber;
        this.issueTitle = issueTitle;
        this.issueBody = issueBody;
        this.issueDate = issueDate;
        this.repository = repository;
        this.account = account;
    }


}
