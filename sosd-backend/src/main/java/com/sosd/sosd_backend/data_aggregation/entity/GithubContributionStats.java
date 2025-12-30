package com.sosd.sosd_backend.data_aggregation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_contribution_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class GithubContributionStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="github_id")
    private Long githubId;
    @Column(name="repo_id")
    private Long repoId;
    private int year;

    private int commitCount;
    private int commitLines;
    private int prCount;
    private int issueCount;

    private double guidelineScore;
    private double repoScore;

    private int starCount;
    private int forkCount;

    private LocalDateTime lastUpdatedAt;

    // -------------------------
    // 정적 팩토리 (생성용)
    // -------------------------
    public static GithubContributionStats createNew(Long githubId, Long repoId, int year) {
        return GithubContributionStats.builder()
                .githubId(githubId)
                .repoId(repoId)
                .year(year)
                .commitCount(0)
                .commitLines(0)
                .prCount(0)
                .issueCount(0)
                .guidelineScore(0.0)
                .repoScore(0.0)
                .starCount(0)
                .forkCount(0)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
    }

    // -------------------------
    // 도메인 메서드 (갱신용)
    // -------------------------
    public void updateStats(int commitCount, int commitLines, int prCount, int issueCount,
                            double guidelineScore, double repoScore,
                            int starCount, int forkCount) {

        this.commitCount = commitCount;
        this.commitLines = commitLines;
        this.prCount = prCount;
        this.issueCount = issueCount;
        this.guidelineScore = guidelineScore;
        this.repoScore = repoScore;
        this.starCount = starCount;
        this.forkCount = forkCount;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}

