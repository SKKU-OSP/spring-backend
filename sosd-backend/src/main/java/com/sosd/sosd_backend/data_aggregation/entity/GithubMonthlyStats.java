package com.sosd.sosd_backend.data_aggregation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Entity
@Table(name = "github_monthly_stats",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_github_monthly_stats",
                columnNames = {"github_id", "start_yymm"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class GithubMonthlyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OSP GithubStatsYymm.github_id(CharField)와 맞추기 위해 login username 저장
    @Column(name = "github_id", nullable = false, length = 40)
    private String githubId;

    @Column(name = "start_yymm", nullable = false)
    private LocalDate startYymm;

    @Column(name = "end_yymm", nullable = false)
    private LocalDate endYymm;

    @Column(name = "stars")
    private int stars;

    @Column(name = "num_of_cr_repos")
    private int numOfCrRepos;

    @Column(name = "num_of_co_repos")
    private int numOfCoRepos;

    @Column(name = "num_of_commits")
    private int numOfCommits;

    // OSP: db_column='num_of_PRs'
    @Column(name = "num_of_PRs")
    private int numOfPRs;

    @Column(name = "num_of_issues")
    private int numOfIssues;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    public static GithubMonthlyStats create(String githubLoginUsername, LocalDate firstDayOfMonth) {
        return GithubMonthlyStats.builder()
                .githubId(githubLoginUsername)
                .startYymm(firstDayOfMonth)
                .endYymm(firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth()))
                .stars(0)
                .numOfCrRepos(0)
                .numOfCoRepos(0)
                .numOfCommits(0)
                .numOfPRs(0)
                .numOfIssues(0)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
    }

    public void updateStats(int commits, int coRepos, int prs, int issues, int crRepos) {
        this.numOfCommits = commits;
        this.numOfCoRepos = coRepos;
        this.numOfPRs = prs;
        this.numOfIssues = issues;
        this.numOfCrRepos = crRepos;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
