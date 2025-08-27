package com.sosd.sosd_backend.github_collector.dto.collect.result;

import java.util.List;

public record CollectResult<T, C extends Cursor>(
        List<T> results,
        C cursor,
        int fetchedCount,
        int totalCount,
        long elapsedTimeMs, // 수집 소요시간
        String source // pr, commit, repo 등등 - 일단은 string으로 처리
) {}
