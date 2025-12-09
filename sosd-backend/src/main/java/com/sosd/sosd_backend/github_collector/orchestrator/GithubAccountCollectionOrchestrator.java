package com.sosd.sosd_backend.github_collector.orchestrator;

import com.sosd.sosd_backend.dto.github.GithubRepositoryUpsertDto;
import com.sosd.sosd_backend.github_collector.collector.RepoCollector;
import com.sosd.sosd_backend.github_collector.dto.collect.context.RepoListCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.TimeCursor;
import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;
import com.sosd.sosd_backend.github_collector.dto.response.GithubRepositoryResponseDto;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.service.github.GithubAccountRepositoryLinkService;
import com.sosd.sosd_backend.service.github.GithubAccountService;
import com.sosd.sosd_backend.service.github.RepoUpsertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.MDC;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubAccountCollectionOrchestrator {

    private final GithubRepositoryOrchestrator githubRepositoryOrchestrator;
    private final RepoCollector repoCollector;
    private final RepoUpsertService repoUpsertService;
    private final GithubAccountRepositoryLinkService linkService;
    private final GithubAccountService githubAccountService;

    /**
     * 단일 깃허브 계정에 대한 수집 수행
     * 해당 깃허브 계정이 기여한 모든 레포에 대해 수집 수행
     * @param githubAccountRef
     */
    public void collectByGithubAccount(GithubAccountRef githubAccountRef){

        log.info(">> Start collection for account: {}", githubAccountRef.githubLoginUsername());

        // 1) 해당 유저가 기여한 모든 레포 수집
        RepoListCollectContext ctx = new RepoListCollectContext(
                githubAccountRef,
                null // TODO: DB의 마지막 수집 시점으로 변경
        );

        try{
            CollectResult<GithubRepositoryResponseDto, TimeCursor> collectedRepos =
                    repoCollector.collect(ctx);

            log.info("[collect] source={} fetched={}/{}(total) elapsed={}ms",
                    collectedRepos.source(),
                    collectedRepos.fetchedCount(),
                    collectedRepos.totalCount(),
                    collectedRepos.elapsedTimeMs()
            );

            for (var repo : collectedRepos.results()) {
                log.info("[collect] found repository={}", repo.fullName());
            }

            // 2) 도메인 모델 뱐환
            List<GithubRepositoryUpsertDto> repoUpsertDtos = collectedRepos.results()
                    .stream()
                    .map(GithubRepositoryUpsertDto::from)
                    .toList();

            // 3) DB 저장
            try{
                // 3-1) 레포 저장
                List<RepoRef> upsertedRepos = repoUpsertService.upsertRepos(repoUpsertDtos);

                log.info("[upsert][acc={}] repos upsert success count={}",
                        githubAccountRef.githubLoginUsername(),
                        upsertedRepos.size()
                );

                // 3-2) 계정 - 레포 링크 테이블 저장
                int linkSuccess = 0;
                for (RepoRef repoRef : upsertedRepos) {
                    try {
                        linkService.linkIfAbsent(githubAccountRef.githubId(), repoRef.repoId());
                        linkSuccess++;
                        log.debug("[link][acc={}] linked repoName={}",
                                githubAccountRef.githubLoginUsername(), repoRef.fullName());
                    } catch (Exception e) {
                        log.warn("[link][acc={}] link failed for repo={}",
                                githubAccountRef.githubLoginUsername(), repoRef.fullName(), e);
                    }
                }
                log.info("[link][acc={}] link ensured count={}/{}",
                        githubAccountRef.githubLoginUsername(), linkSuccess, upsertedRepos.size());

            } catch (Exception e) {
                log.error("[upsert] repo upsert failed", e);
            }

        } catch (Exception e) {
            log.error("[collect] repo collect failed", e);
        }

        // 4) 이 계정과 연관된 모든 레포 가져오기
        List<RepoRef> allLinkedRepoRefs = new ArrayList<>();
        try {
            allLinkedRepoRefs = linkService.listRepoRefs(githubAccountRef.githubId());
            log.info("[link] target repo count={}", allLinkedRepoRefs.size());
        } catch (Exception e) {
            log.error("[link] link failed", e);
        }

        // 5) 하위 orchestrator 수집
        for(RepoRef repoRef : allLinkedRepoRefs){
            MDC.put("repoCtx", "Repo:" + repoRef.fullName());
            try {

                LocalDateTime lastCrawling = linkService.getLastUpdatedAt(
                        githubAccountRef.githubId(),
                        repoRef.repoId()
                ).orElse(null);
                LocalDateTime updatedAt = repoRef.githubRepositoryUpdatedAt();
                LocalDateTime pushedAt = repoRef.githubPushedAt();
                LocalDateTime lastChangedAt = Stream.of(updatedAt, pushedAt)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(updatedAt);

                // 5-1 최종 크롤링 이후 업데이트가 없다면 스킵
                if (lastCrawling != null && !lastChangedAt.isAfter(lastCrawling)) {
                    log.info("Skip collection for repository (no updates since last crawl)");
                    continue;
                }

                // 5-2 수집 실행
                githubRepositoryOrchestrator.collectByRepository(githubAccountRef, repoRef);
            } finally {
                MDC.remove("repoCtx");
            }
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            githubAccountService.updateLastCrawling(githubAccountRef.githubId(), now);
            log.info("[update] last crawling updated at {}", now);
        } catch (Exception e) {
            log.error("[update] last crawling update failed", e);
        }

        log.info("<< End collection for account: {}", githubAccountRef.githubLoginUsername());
    }

}
