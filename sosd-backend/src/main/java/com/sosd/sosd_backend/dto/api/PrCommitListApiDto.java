package com.sosd.sosd_backend.dto.api;

import lombok.Builder;

import java.util.List;

/** PR 정합성 에이전트가 탐색에 사용하는 가벼운 커밋 목록 DTO. */
@Builder
public record PrCommitListApiDto(
        String owner,
        String repo,
        int prNumber,
        int totalCount,
        List<CommitSummary> commits
) {
    @Builder
    public record CommitSummary(
            String sha,
            String messageHeadline,
            Integer fileCount,
            int additions,
            int deletions
    ) {}
}
