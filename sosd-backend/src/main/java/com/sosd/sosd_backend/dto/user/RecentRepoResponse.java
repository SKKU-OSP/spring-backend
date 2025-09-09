package com.sosd.sosd_backend.dto.user;

import java.time.LocalDateTime;

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
