package com.sosd.sosd_backend.ai_evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.sosd_backend.ai_evaluation.entity.GithubRepoAiEvaluationEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AiEvaluationResponse(
        @JsonProperty("score") String score,
        @JsonProperty("total_score") Double totalScore,
        @JsonProperty("criteria_scores") Map<String, CriterionScore> criteriaScores,
        @JsonProperty("missing_essentials") List<String> missingEssentials,
        @JsonProperty("strengths") List<String> strengths,
        @JsonProperty("improvements") List<String> improvements,
        @JsonProperty("advice") List<String> advice,
        @JsonProperty("updated_at") LocalDateTime updatedAt,
        @JsonProperty("readme") String readme
) {
    public record CriterionScore(
            @JsonProperty("score") int score,
            @JsonProperty("reason") String reason
    ) {}

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, CriterionScore>> CRITERIA_TYPE =
            new TypeReference<>() {};

    private static Map<String, CriterionScore> parseCriteriaScores(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return MAPPER.readValue(json, CRITERIA_TYPE);
        } catch (Exception e) {
            return null;
        }
    }

    public static AiEvaluationResponse from(GithubRepoAiEvaluationEntity entity) {
        return new AiEvaluationResponse(
                entity.getReadmeScore(),
                entity.getReadmeTotalScore(),
                parseCriteriaScores(entity.getReadmeCriteriaScores()),
                entity.getReadmeMissingEssentials(),
                entity.getReadmeStrengths(),
                entity.getReadmeImprovements(),
                entity.getReadmeAdvice(),
                entity.getUpdatedAt(),
                null
        );
    }

    public static AiEvaluationResponse from(GithubRepoAiEvaluationEntity entity, String readme) {
        return new AiEvaluationResponse(
                entity.getReadmeScore(),
                entity.getReadmeTotalScore(),
                parseCriteriaScores(entity.getReadmeCriteriaScores()),
                entity.getReadmeMissingEssentials(),
                entity.getReadmeStrengths(),
                entity.getReadmeImprovements(),
                entity.getReadmeAdvice(),
                entity.getUpdatedAt(),
                readme
        );
    }
}