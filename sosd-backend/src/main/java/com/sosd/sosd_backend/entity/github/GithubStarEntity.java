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
@Table(name = "github_star")
public class GithubStarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Star한 사용자 (github_account 테이블 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "star_user_id", nullable = false)
    private GithubAccount starUser;

    @Column(name = "star_date", nullable = false)
    private LocalDateTime starDate;

    // repo_id → github_repository.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id", nullable = false)
    private GithubRepositoryEntity repository;

    @Builder
    public GithubStarEntity(
            Long id,
            GithubAccount starUser,
            LocalDateTime starDate,
            GithubRepositoryEntity repository
    ) {
        this.id = id;
        this.starUser = starUser;
        this.starDate = starDate;
        this.repository = repository;
    }

}
