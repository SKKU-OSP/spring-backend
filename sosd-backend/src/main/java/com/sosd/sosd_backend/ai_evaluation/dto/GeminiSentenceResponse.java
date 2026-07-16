package com.sosd.sosd_backend.ai_evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeminiSentenceResponse(
        @JsonProperty("strengths") List<String> strengths,
        @JsonProperty("improvements") List<String> improvements,
        @JsonProperty("advice") List<String> advice
) {}