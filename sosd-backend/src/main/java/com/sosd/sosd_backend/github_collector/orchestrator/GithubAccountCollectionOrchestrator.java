package com.sosd.sosd_backend.github_collector.orchestrator;

import com.sosd.sosd_backend.dto.github.GithubRepositoryUpsertDto;
import com.sosd.sosd_backend.github_collector.collector.RepoCollector;
import com.sosd.sosd_backend.github_collector.dto.collect.context.RepoListCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.TimeCursor;
import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;
import com.sosd.sosd_backend.github_collector.dto.response.GithubRepositoryResponseDto;
import com.sosd.sosd_backend.service.github.RepoUpsertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubAccountCollectionOrchestrator {

    private final GithubRepositoryOrchestrator githubRepositoryOrchestrator;
    private final RepoCollector repoCollector;
    private final RepoUpsertService repoUpsertService;

    /**
     * 단일 깃허브 계정에 대한 수집 수행
     * 해당 깃허브 계정이 기여한 모든 레포에 대해 수집 수행
     * @param githubAccountRef
     */
    public void collectByGithubAccount(GithubAccountRef githubAccountRef){
        List<RepoRef> repoRefs = new ArrayList<>();

        // 1) 해당 유저가 기여한 모든 레포 수집
        RepoListCollectContext ctx = new RepoListCollectContext(
                githubAccountRef,
                null // TODO: DB의 마지막 수집 시점으로 변경
        );

        try{
            CollectResult<GithubRepositoryResponseDto, TimeCursor> collectedRepos =
                    repoCollector.collect(ctx);

            log.info("[collect][acc={}] source={} fetched={}/{}(total) elapsed={}ms",
                    githubAccountRef.githubLoginUsername(),
                    collectedRepos.source(),
                    collectedRepos.fetchedCount(),
                    collectedRepos.totalCount(),
                    collectedRepos.elapsedTimeMs()
            );

            for (var repo : collectedRepos.results()) {
                log.info("[collect][acc={}] source={} repository={}",
                        githubAccountRef.githubLoginUsername(),
                        collectedRepos.source(),
                        repo.fullName()
                );
                }

            List<GithubRepositoryResponseDto> repoResponseDtos = collectedRepos.results();

            // 2) 도메인 모델 뱐환
            List<GithubRepositoryUpsertDto> repoUpsertDtos = repoResponseDtos.stream()
                    .map(GithubRepositoryUpsertDto::from)
                    .toList();
            try{
                // 3) DB 저장
                repoRefs = repoUpsertService.upsertRepos(repoUpsertDtos);

                log.info("[upsert][acc={}] repos upsert success count={}",
                        githubAccountRef.githubLoginUsername(),
                        repoRefs.size()
                );
            } catch (Exception e) {
                log.error("[upsert][acc={}] repo upsert failed", githubAccountRef.githubLoginUsername(), e);
            }

        } catch (Exception e) {
            log.error("[collect][acc={}] repo collect failed", githubAccountRef.githubLoginUsername(), e);
        }

        // 4) 하위 orchestrator 수집
        for(RepoRef repoRef : repoRefs){
            githubRepositoryOrchestrator.collectByRepository(githubAccountRef, repoRef);
        }
    }



}
