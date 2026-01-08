package com.sosd.sosd_backend.service;

/**
 * github_contribution_stats 집계용 프로젝션
 * JPQL SUM()은 Long을 반환하므로 Long 타입 사용
 */
public record ContributionAggregate(
        Long commitCount,
        Long commitLines,
        Long prCount,
        Long issueCount,
        Long starCount,
        Long forkCount,
        Double guidelineScore
) {}
