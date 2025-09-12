package com.sosd.sosd_backend.dto.github;

import java.time.LocalDateTime;

/** Star Upsert용 Command DTO (UTC 정규화, FK는 id 형태) */
public record GithubStarUpsertDto(
        Long starUserGithubId, // github_account.github_id
        LocalDateTime starDateUtc,
        Long repositoryId      // github_repository.id
) {}