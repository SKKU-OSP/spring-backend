package com.sosd.sosd_backend.entity.github;

import com.sosd.sosd_backend.dto.github.GithubPullRequestUpsertDto;
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

    // 신규 엔티티 생성 정적 팩토리 메서드
    public static GithubPullRequestEntity create(
            GithubPullRequestUpsertDto d,
            GithubRepositoryEntity repository,
            GithubAccount account
    ) {
        requireNonNull(d, "dto is null");
        requireNonNull(repository, "repository is null");
        requireNonNull(account, "account is null");

        Assert.notNull(d.githubPrId(), "githubPrId is null");
        Assert.notNull(d.prNumber(), "prNumber is null");
        Assert.hasText(d.prTitle(), "prTitle is empty");
        requireNonNull(d.prDateUtc(), "prDateUtc is null");
        requireNonNull(d.isOpen(), "isOpen is null");

        return GithubPullRequestEntity.builder()
                .githubPrId(d.githubPrId())
                .prNumber(d.prNumber())
                .prTitle(d.prTitle())
                .prBody(d.prBody())
                .prDate(d.prDateUtc())
                .isOpen(d.isOpen())
                .repository(repository)
                .account(account)
                .build();
    }

    // ===== 업서트 Update: 허용된 범위만 갱신 =====
    public void applyUpsert(GithubPullRequestUpsertDto d) {
        if (d == null) return;

        // githubPrId, repo, account는 불변
        if (d.prNumber() != null && !d.prNumber().equals(this.prNumber)) {
            this.prNumber = d.prNumber();
        }

        if (d.prTitle() != null && !d.prTitle().equals(this.prTitle)) {
            this.prTitle = d.prTitle();
        }

        if (!java.util.Objects.equals(this.prBody, d.prBody())) {
            this.prBody = d.prBody();
        }

        if (d.prDateUtc() != null && !d.prDateUtc().equals(this.prDate)) {
            this.prDate = d.prDateUtc();
        }

        if (d.isOpen() != null && !d.isOpen().equals(this.isOpen)) {
            this.isOpen = d.isOpen();
        }
    }
}

