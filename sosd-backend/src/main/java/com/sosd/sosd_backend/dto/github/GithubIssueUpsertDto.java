package com.sosd.sosd_backend.dto.github;

import java.time.LocalDateTime;

/** Issue Upsert용 Command DTO (UTC 정규화, FK는 id 형태) */
public record GithubIssueUpsertDto(
        Long githubIssueId,
        Integer issueNumber,
        String issueTitle,
        String issueBody,
        LocalDateTime issueDateUtc,
        Long repositoryId,     // github_repository.id
        Long accountGithubId   // github_account.github_id
) {}