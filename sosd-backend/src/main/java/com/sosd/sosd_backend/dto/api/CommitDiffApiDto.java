package com.sosd.sosd_backend.dto.api;

import lombok.Builder;

import java.util.List;

/** Django로 반환하는 커밋 diff DTO */
@Builder
public record CommitDiffApiDto(
        String sha,
        String owner,
        String repo,
        List<FileDiff> files
) {
    @Builder
    public record FileDiff(
            String filename,
            String status,
            int additions,
            int deletions,
            String patch
    ) {}
}