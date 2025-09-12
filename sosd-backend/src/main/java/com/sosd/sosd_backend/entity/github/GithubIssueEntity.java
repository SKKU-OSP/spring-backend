package com.sosd.sosd_backend.entity.github;


import com.sosd.sosd_backend.dto.github.GithubIssueUpsertDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

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

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen;

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
            boolean isOpen,
            GithubRepositoryEntity repository,
            GithubAccount account
    ) {
        this.githubIssueId = githubIssueId;
        this.issueNumber = issueNumber;
        this.issueTitle = issueTitle;
        this.issueBody = issueBody;
        this.issueDate = issueDate;
        this.isOpen = isOpen;
        this.repository = repository;
        this.account = account;
    }

    // ===== 정적 팩토리 메서드 =====
    public static GithubIssueEntity create(
            GithubIssueUpsertDto d,
            GithubRepositoryEntity repository,
            GithubAccount account
    ) {
        requireNonNull(d, "dto is null");
        requireNonNull(repository, "repository is null");
        requireNonNull(account, "account is null");

        Assert.notNull(d.githubIssueId(), "githubIssueId is null");
        Assert.notNull(d.issueNumber(), "issueNumber is null");
        Assert.hasText(d.issueTitle(), "issueTitle is empty");
        requireNonNull(d.issueDateUtc(), "issueDateUtc is null");
        requireNonNull(d.isOpen(), "isOpen is null");

        return GithubIssueEntity.builder()
                .githubIssueId(d.githubIssueId())
                .issueNumber(d.issueNumber())
                .issueTitle(d.issueTitle())
                .issueBody(d.issueBody())
                .issueDate(d.issueDateUtc())   // UTC로 정규화된 값
                .isOpen(d.isOpen())
                .repository(repository)
                .account(account)
                .build();
    }

    // ===== 업서트 Update: 허용된 범위만 갱신 =====
    public void applyUpsert(GithubIssueUpsertDto d) {
        if (d == null) return;

        // githubIssueId, repo, account는 불변
        if (d.issueNumber() != null && !d.issueNumber().equals(this.issueNumber)) {
            this.issueNumber = d.issueNumber();
        }

        if (d.issueTitle() != null && !d.issueTitle().equals(this.issueTitle)) {
            this.issueTitle = d.issueTitle();
        }

        if (!java.util.Objects.equals(this.issueBody, d.issueBody())) {
            this.issueBody = d.issueBody();
        }

        if (d.issueDateUtc() != null && !d.issueDateUtc().equals(this.issueDate)) {
            this.issueDate = d.issueDateUtc();
        }

        if (d.isOpen() != null && !d.isOpen().equals(this.isOpen)) {
            this.isOpen = d.isOpen();
        }
    }


}
