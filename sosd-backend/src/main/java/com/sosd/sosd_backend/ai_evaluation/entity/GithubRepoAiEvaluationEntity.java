package com.sosd.sosd_backend.ai_evaluation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "github_repo_ai_evaluation")
public class GithubRepoAiEvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_id", nullable = false)
    private String githubLoginUsername;

    @Column(name = "repo_name", nullable = false)
    private String repoName;

    @Column(name = "readme_score", length = 10)
    private String readmeScore;

    @Column(name = "readme_missing_essentials", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    private List<String> readmeMissingEssentials;

    @Column(name = "readme_strengths", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    private List<String> readmeStrengths;

    @Column(name = "readme_improvements", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    private List<String> readmeImprovements;

    @Column(name = "readme_advice", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    private List<String> readmeAdvice;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public GithubRepoAiEvaluationEntity(
            String githubLoginUsername,
            String repoName,
            String readmeScore,
            List<String> readmeMissingEssentials,
            List<String> readmeStrengths,
            List<String> readmeImprovements,
            List<String> readmeAdvice
    ) {
        this.githubLoginUsername = githubLoginUsername;
        this.repoName = repoName;
        this.readmeScore = readmeScore;
        this.readmeMissingEssentials = readmeMissingEssentials;
        this.readmeStrengths = readmeStrengths;
        this.readmeImprovements = readmeImprovements;
        this.readmeAdvice = readmeAdvice;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateReadmeEvaluation(
            String readmeScore,
            List<String> readmeMissingEssentials,
            List<String> readmeStrengths,
            List<String> readmeImprovements,
            List<String> readmeAdvice
    ) {
        this.readmeScore = readmeScore;
        this.readmeMissingEssentials = readmeMissingEssentials;
        this.readmeStrengths = readmeStrengths;
        this.readmeImprovements = readmeImprovements;
        this.readmeAdvice = readmeAdvice;
        this.updatedAt = LocalDateTime.now();
    }
}
