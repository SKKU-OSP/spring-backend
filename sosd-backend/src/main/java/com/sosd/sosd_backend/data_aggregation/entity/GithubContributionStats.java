package com.sosd.sosd_backend.data_aggregation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_contribution_stats")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubContributionStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long githubId;
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
}

