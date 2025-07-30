package com.sosd.sosd_backend.entity.github;

import com.sosd.sosd_backend.entity.user.UserAccount;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "github_account")
public class GithubAccount {

    // 기본키
    @Id
    @Column(name = "github_id")
    private Long githubId;

    // 일반 컬럼
    @Column(name = "github_login", nullable = false)
    private String githubLogin;

    @Column(name = "github_name")
    private String githubName;

    @Column(name = "github_token")
    private String githubToken;

    @Column(name = "github_email", nullable = false)
    private String githubEmail;

    @Column(name = "last_crawling")
    private LocalDateTime lastCrawling;

    // 연관관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount userAccount;

    // 생성자
    @Builder
    public GithubAccount(
            Long githubId,
            String githubLogin,
            String githubName,
            String githubToken,
            String githubEmail,
            UserAccount userAccount
    ) {
        this.githubId = githubId;
        this.githubLogin = githubLogin;
        this.githubName = githubName;
        this.githubToken = githubToken;
        this.githubEmail = githubEmail;
        this.lastCrawling = null;
        this.userAccount = userAccount;
    }

    // 수집기 작동 시 호출 메소드
    public void updateLastCrawling() {
        this.lastCrawling = LocalDateTime.now();
    }

}
