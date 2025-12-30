package com.sosd.sosd_backend.data_aggregation.dto;

import java.time.LocalDateTime;

public record AccountRepoProjection(
        Long githubId,
        Long repoId,
        String readme,
        String license,
        String description,
        Integer star,
        Integer fork,
        LocalDateTime lastUpdatedAt
) {}
