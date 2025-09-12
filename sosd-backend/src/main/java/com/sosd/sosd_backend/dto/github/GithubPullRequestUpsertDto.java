package com.sosd.sosd_backend.dto.github;

import java.time.LocalDateTime;

/** PR Upsert용 Command DTO (UTC 정규화, FK는 id 형태) */
public record GithubPullRequestUpsertDto(
        Long githubPrId,
        Integer prNumber,
        String prTitle,
        String prBody,
        LocalDateTime prDateUtc,
        Boolean isOpen,
        Long repositoryId,     // github_repository.id
        Long accountGithubId   // github_account.github_id
) {}