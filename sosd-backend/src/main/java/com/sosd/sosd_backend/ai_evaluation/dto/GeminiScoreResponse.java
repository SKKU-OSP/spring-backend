package com.sosd.sosd_backend.ai_evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeminiScoreResponse(
        @JsonProperty("clarity") ClarityScore clarity,
        @JsonProperty("reproducibility_result") CriterionScore reproducibilityResult,
        @JsonProperty("collaboration") CriterionScore collaboration,
        @JsonProperty("missing_essentials") List<String> missingEssentials
) {
    public record ClarityScore(
            @JsonProperty("score") int score,
            @JsonProperty("reason") String reason,
            @JsonProperty("satisfied_subs") List<String> satisfiedSubs,
            @JsonProperty("unsatisfied_subs") List<String> unsatisfiedSubs
    ) {}

    public record CriterionScore(
            @JsonProperty("score") int score,
            @JsonProperty("reason") String reason
    ) {}
}
