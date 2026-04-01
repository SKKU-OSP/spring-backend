package com.sosd.sosd_backend.github_collector.scheduler;

import com.sosd.sosd_backend.data_aggregation.launcher.StatsJobLauncher;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.user.UserAccount;
import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.orchestrator.GithubAccountCollectionOrchestrator;
import com.sosd.sosd_backend.github_collector.orchestrator.RepositoryBasedCollectionOrchestrator;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.repository.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubSimpleScheduler {

    private final UserAccountRepository userAccountRepository;
    private final GithubAccountRepository githubAccountRepository;
    private final GithubAccountCollectionOrchestrator accountCollectionOrchestrator;
    private final RepositoryBasedCollectionOrchestrator repositoryBasedCollectionOrchestrator;
    private final StatsJobLauncher statsJobLauncher;

    /**
     * OSP 방식 2단계 수집
     *
     * 1단계: 모든 사용자의 레포 탐색 및 등록 (GitHub API → DB)
     * 2단계: 등록된 레포 기준으로 모든 계정의 커밋/PR/이슈 수집
     */
    public void run() {
        log.info("Start Github Simple Scheduler");

        List<UserAccount> userAccounts = userAccountRepository.findAllByIsActiveTrue();
        int totalCount = userAccounts.size();

        // 1단계: 모든 사용자의 레포 발견 + 링크 생성
        log.info("=== Phase 1: Repo Discovery ({} users) ===", totalCount);
        int currentIdx = 0;
        for (UserAccount userAccount : userAccounts) {
            currentIdx++;
            log.info("Current Progress: {}/{} users", currentIdx, totalCount);

            MDC.put("userCtx", "User:" + userAccount.getStudentId());
            try {
                List<GithubAccount> githubAccounts =
                        githubAccountRepository.findAllByUserAccount_StudentId(userAccount.getStudentId());
                for (GithubAccount githubAccount : githubAccounts) {
                    MDC.put("githubCtx", "Acc:" + githubAccount.getGithubLoginUsername());
                    try {
                        GithubAccountRef ref = githubAccount.toGithubAccountRef();
                        accountCollectionOrchestrator.discoverRepos(ref);
                    } finally {
                        MDC.remove("githubCtx");
                    }
                }
            } finally {
                MDC.remove("userCtx");
            }
        }

        // 2단계: 등록된 레포 기준으로 커밋/PR/이슈 수집
        log.info("=== Phase 2: Repo-based Commit Collection ===");
        repositoryBasedCollectionOrchestrator.collectAll();

        // 3단계: 수집 완료 후 월별 통계 집계
        log.info("=== Phase 3: Monthly Stats Aggregation ===");
        statsJobLauncher.runMonthlyStatsJob();

        log.info("End Github Simple Scheduler");
    }
}
