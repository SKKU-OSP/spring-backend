package com.sosd.sosd_backend.entity.github;

import com.sosd.sosd_backend.dto.github.GithubCommitUpsertDto;
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

    @Builder(access =  AccessLevel.PRIVATE)
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

    // 신규 엔티티 생성 정적 팩토리 메서드
    public static GithubCommitEntity create(
            GithubCommitUpsertDto d,
            GithubRepositoryEntity repository,
            GithubAccount account
    ) {
        requireNonNull(d, "dto is null");
        requireNonNull(repository, "repository is null");
        requireNonNull(account, "account is null");
        Assert.hasText(d.sha(), "sha is empty");
        requireNonNull(d.authorDateUtc(), "authorDateUtc is null");
        requireNonNull(d.committerDateUtc(), "committerDateUtc is null");

        return GithubCommitEntity.builder()
                .sha(d.sha())
                .addition(Math.max(0, d.addition()))
                .deletion(Math.max(0, d.deletion()))
                .authorDate(d.authorDateUtc())     // 이미 UTC로 정규화된 값
                .committerDate(d.committerDateUtc())
                .message(d.message())
                .repository(repository)
                .account(account)
                .build();
    }

    // ---- 업서트 Update: 허용된 범위만 부분 갱신 ----
    public void applyUpsert(GithubCommitUpsertDto d) {
        if (d == null) return;

        // 커밋은 보통 불변이므로 sha/repo/account는 변경하지 않음
        int newAdd = Math.max(0, d.addition());
        int newDel = Math.max(0, d.deletion());
        if (this.addition != newAdd) this.addition = newAdd;
        if (this.deletion != newDel) this.deletion = newDel;

        // 작성/커밋 시각은 일반적으로 불변
        if (d.authorDateUtc() != null && !d.authorDateUtc().equals(this.authorDate)) {
            this.authorDate = d.authorDateUtc();
        }
        if (d.committerDateUtc() != null && !d.committerDateUtc().equals(this.committerDate)) {
            this.committerDate = d.committerDateUtc();
        }
        if (!java.util.Objects.equals(this.message, d.message())) {
            this.message = d.message();
        }
    }
}
