package com.sosd.sosd_backend.entity.github;

import com.sosd.sosd_backend.entity.user.UserAccount;
import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    @Column(name = "github_graphql_node_id", nullable = false)
    private String githubGraphqlNodeId;

    @Column(name = "github_login_username", nullable = false)
    private String githubLoginUsername;

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

    // 계정 - 조인테이블 - 레포지토리
    @OneToMany(mappedBy = "githubAccount", cascade = CascadeType.PERSIST, orphanRemoval = false)
    private Set<GithubAccountRepositoryEntity> accountRepositories = new HashSet<>();

    // 생성자
    @Builder
    public GithubAccount(
            Long githubId,
            String githubGraphqlNodeId,
            String githubLoginUsername,
            String githubName,
            String githubToken,
            String githubEmail,
            UserAccount userAccount
    ) {
        this.githubId = githubId;
        this.githubGraphqlNodeId = githubGraphqlNodeId;
        this.githubLoginUsername = githubLoginUsername;
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

    // 엔티티 -> githubAccountRef 변환 메서드
    public GithubAccountRef toGithubAccountRef(){
        return new GithubAccountRef(
                this.githubId,
                this.githubGraphqlNodeId,
                this.githubLoginUsername,
                this.githubName,
                this.githubToken,
                this.githubEmail
        );
    }

}
