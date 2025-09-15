package com.sosd.sosd_backend.dto.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public record DashboardContributionResponse(
        Long repoNum,
        Long commits,
        Long commitLines,
        Long issues,
        Long prs
) {}
