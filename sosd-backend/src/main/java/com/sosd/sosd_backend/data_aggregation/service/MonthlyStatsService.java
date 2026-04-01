package com.sosd.sosd_backend.data_aggregation.service;

import com.sosd.sosd_backend.data_aggregation.entity.GithubMonthlyStats;
import com.sosd.sosd_backend.data_aggregation.repository.*;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyStatsService {

    private final AggregationGithubCommitRepository commitRepository;
    private final AggregationGithubPullRequestRepository prRepository;
    private final AggregationGithubIssueRepository issueRepository;
    private final AggregationGithubRepositoryRepository repoRepository;

    /**
     * 계정의 전체 월별 기여 통계를 계산하여 반환.
     * yymm key: "YYYY-MM-01"
     */
    public List<GithubMonthlyStats> calculateMonthlyStats(GithubAccount account) {
        Long githubId = account.getGithubId();
        String loginUsername = account.getGithubLoginUsername();

        log.info("월별 통계 계산: githubId={}, username={}", githubId, loginUsername);

        // [yymm -> [commits, coRepos, prs, issues, crRepos]]
        Map<String, int[]> statsMap = new HashMap<>();

        // 커밋 + co_repos
        for (Object[] row : commitRepository.findMonthlyCommitStatsByGithubId(githubId)) {
            String yymm = (String) row[0];
            int commits = ((Number) row[1]).intValue();
            int coRepos = ((Number) row[2]).intValue();
            statsMap.computeIfAbsent(yymm, k -> new int[5]);
            statsMap.get(yymm)[0] = commits;
            statsMap.get(yymm)[1] = coRepos;
        }

        // PRs
        for (Object[] row : prRepository.findMonthlyPrStatsByGithubId(githubId)) {
            String yymm = (String) row[0];
            int prs = ((Number) row[1]).intValue();
            statsMap.computeIfAbsent(yymm, k -> new int[5]);
            statsMap.get(yymm)[2] = prs;
        }

        // Issues
        for (Object[] row : issueRepository.findMonthlyIssueStatsByGithubId(githubId)) {
            String yymm = (String) row[0];
            int issues = ((Number) row[1]).intValue();
            statsMap.computeIfAbsent(yymm, k -> new int[5]);
            statsMap.get(yymm)[3] = issues;
        }

        // 생성 레포 (cr_repos)
        for (Object[] row : repoRepository.findMonthlyCrReposByLoginUsername(loginUsername)) {
            String yymm = (String) row[0];
            int crRepos = ((Number) row[1]).intValue();
            statsMap.computeIfAbsent(yymm, k -> new int[5]);
            statsMap.get(yymm)[4] = crRepos;
        }

        List<GithubMonthlyStats> results = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : statsMap.entrySet()) {
            LocalDate firstDay = LocalDate.parse(entry.getKey());
            int[] v = entry.getValue();
            GithubMonthlyStats stats = GithubMonthlyStats.create(loginUsername, firstDay);
            stats.updateStats(v[0], v[1], v[2], v[3], v[4]);
            results.add(stats);
        }

        log.info("월별 통계 계산 완료: username={}, {}개월", loginUsername, results.size());
        return results;
    }
}
