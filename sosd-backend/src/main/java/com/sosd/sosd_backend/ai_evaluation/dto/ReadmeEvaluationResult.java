package com.sosd.sosd_backend.ai_evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ReadmeEvaluationResult(
        @JsonProperty("score") String score,
        @JsonProperty("missing_essentials") List<String> missingEssentials,
        @JsonProperty("strengths") List<String> strengths,
        @JsonProperty("improvements") List<String> improvements,
        @JsonProperty("advice") List<String> advice
) {}
