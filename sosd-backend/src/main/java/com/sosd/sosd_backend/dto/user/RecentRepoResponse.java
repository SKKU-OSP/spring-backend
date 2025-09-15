package com.sosd.sosd_backend.dto.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public record RecentRepoResponse(
        String repoName,
        Long githubId,
        LocalDateTime committerDate,
        String desc,
        Integer stargazersCount,
        Long commitsCount,
        Long prsCount
) {
}
