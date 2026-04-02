package com.sosd.sosd_backend.dto.github;

import java.time.LocalDateTime;

/** 커밋 Upsert용 Command DTO (UTC로 정규화, FK는 id 형태) */
public record GithubCommitUpsertDto(
        String sha,
        int addition,
        int deletion,
        LocalDateTime authorDateUtc,
        LocalDateTime committerDateUtc,
        String message,
        String authorLogin,        // github_account.github_login_username (author)
        Long repositoryId,         // github_repository.id
        Long accountGithubId       // github_account.github_id
) {}
