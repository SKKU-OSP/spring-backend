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
@Table(name = "github_commit")
public class GithubCommitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sha", nullable = false)
    private String sha;

    @Column(name = "addition", nullable = false)
    private int addition;

    @Column(name = "deletion", nullable = false)
    private int deletion;

    @Column(name = "author_date", nullable = false)
    private LocalDateTime authorDate;

    @Column(name = "committer_date", nullable = false)
    private LocalDateTime committerDate;

    @Column(name = "message")
    private String message;

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
    public GithubCommitEntity(
            Long id,
            String sha,
            int addition,
            int deletion,
            LocalDateTime authorDate,
            LocalDateTime committerDate,
            String message,
            GithubRepositoryEntity repository,
            GithubAccount account
    ){
        this.id = id;
        this.sha = sha;
        this.addition = addition;
        this.deletion = deletion;
        this.authorDate = authorDate;
        this.committerDate = committerDate;
        this.message = message;
        this.repository = repository;
        this.account = account;
    }

}
