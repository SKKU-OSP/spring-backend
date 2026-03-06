package com.sosd.sosd_backend.github_collector.orchestrator;

import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;
import com.sosd.sosd_backend.service.github.GithubAccountRepositoryLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * OSP 방식 수집기: 레포 기준으로 수집
 *
 * 기존 방식 (사용자 중심):
 *   사용자 → 기여 레포 탐색(API) → 각 레포에서 해당 사용자 커밋 수집
 *
 * OSP 방식 (레포 중심):
 *   등록된 레포 → 링크된 모든 계정 → 각 계정의 커밋/PR/이슈 수집
 *
 * 이 방식의 장점:
 *   - 공동 기여 레포 기준으로 공정하게 수집
 *   - 개인 private 레포가 등록되지 않으면 수집에서 제외됨
 *   - 레포 단위로 모든 기여자를 한 번에 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RepositoryBasedCollectionOrchestrator {

    private final GithubRepositoryOrchestrator githubRepositoryOrchestrator;
    private final GithubAccountRepositoryLinkService linkService;

    /**
     * 시스템에 등록된 모든 레포에 대해 수집 수행
     */
    public void collectAll() {
        List<RepoRef> allRepos = linkService.listAllLinkedRepos();
        log.info(">>> Start repo-based collection. total repos={}", allRepos.size());

        int repoIdx = 0;
        for (RepoRef repoRef : allRepos) {
            repoIdx++;
            MDC.put("repoCtx", "Repo:" + repoRef.fullName());
            try {
                log.info("[{}/{}] collecting repo={}", repoIdx, allRepos.size(), repoRef.fullName());
                collectByRepo(repoRef);
            } finally {
                MDC.remove("repoCtx");
            }
        }

        log.info("<<< End repo-based collection. total repos={}", allRepos.size());
    }

    /**
     * 단일 레포에 대해 링크된 모든 계정의 커밋/PR/이슈 수집
     */
    private void collectByRepo(RepoRef repoRef) {
        List<GithubAccountRef> accounts = linkService.listAccountRefsByRepo(repoRef.repoId());
        log.info("[repo={}] linked accounts={}", repoRef.fullName(), accounts.size());

        for (GithubAccountRef accountRef : accounts) {
            MDC.put("accCtx", "Acc:" + accountRef.githubLoginUsername());
            try {
                LocalDateTime lastCrawling = linkService.getLastUpdatedAt(
                        accountRef.githubId(),
                        repoRef.repoId()
                ).orElse(null);

                LocalDateTime updatedAt = repoRef.githubRepositoryUpdatedAt();
                LocalDateTime pushedAt = repoRef.githubPushedAt();
                LocalDateTime lastChangedAt = Stream.of(updatedAt, pushedAt)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(updatedAt);

                if (lastCrawling != null && !lastChangedAt.isAfter(lastCrawling)) {
                    log.info("[repo={}][acc={}] skip (no updates since last crawl)",
                            repoRef.fullName(), accountRef.githubLoginUsername());
                    continue;
                }

                githubRepositoryOrchestrator.collectByRepository(accountRef, repoRef);
            } finally {
                MDC.remove("accCtx");
            }
        }
    }
}
