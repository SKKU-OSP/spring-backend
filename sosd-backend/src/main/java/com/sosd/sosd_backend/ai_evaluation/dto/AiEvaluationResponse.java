package com.sosd.sosd_backend.ai_evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosd.sosd_backend.ai_evaluation.entity.GithubRepoAiEvaluationEntity;

import java.time.LocalDateTime;
import java.util.List;

public record AiEvaluationResponse(
        @JsonProperty("score") String score,
        @JsonProperty("missing_essentials") List<String> missingEssentials,
        @JsonProperty("strengths") List<String> strengths,
        @JsonProperty("improvements") List<String> improvements,
        @JsonProperty("advice") List<String> advice,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {
    public static AiEvaluationResponse from(GithubRepoAiEvaluationEntity entity) {
        return new AiEvaluationResponse(
                entity.getReadmeScore(),
                entity.getReadmeMissingEssentials(),
                entity.getReadmeStrengths(),
                entity.getReadmeImprovements(),
                entity.getReadmeAdvice(),
                entity.getUpdatedAt()
        );
    }
}
