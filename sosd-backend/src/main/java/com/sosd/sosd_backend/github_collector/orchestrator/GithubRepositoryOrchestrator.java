package com.sosd.sosd_backend.github_collector.orchestrator;

import com.sosd.sosd_backend.github_collector.collector.CommitCollector;
import com.sosd.sosd_backend.github_collector.collector.IssueCollector;
import com.sosd.sosd_backend.github_collector.collector.PullRequestCollector;
import com.sosd.sosd_backend.github_collector.dto.collect.context.CommitCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.context.IssueCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.context.PullRequestCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.ShaCursor;
import com.sosd.sosd_backend.github_collector.dto.collect.result.TimeCursor;
import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;
import com.sosd.sosd_backend.github_collector.dto.response.GithubCommitResponseDto;
import com.sosd.sosd_backend.github_collector.dto.response.GithubIssueResponseDto;
import com.sosd.sosd_backend.github_collector.dto.response.GithubPullRequestResponseDto;
import com.sosd.sosd_backend.service.github.CommitUpsertService;
import com.sosd.sosd_backend.service.github.IssueUpsertService;
import com.sosd.sosd_backend.service.github.PullRequestUpsertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;
import org.slf4j.MDC;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubRepositoryOrchestrator {

    // collector
    private final CommitCollector  commitCollector;
    private final PullRequestCollector pullRequestCollector;
    private final IssueCollector  issueCollector;

    // upsert service
    private final CommitUpsertService commitUpsertService;
    private final PullRequestUpsertService pullRequestUpsertService;
    private final IssueUpsertService issueUpsertService;

    public void collectByRepository(GithubAccountRef githubAccountRef, RepoRef repoRef){

        log.info("> Start collection for repository: {}", repoRef.fullName());

        // TODO: DB에서 마지막 수집 시점 가져오는걸로 변경
        CommitCollectContext commitCollectContext = new CommitCollectContext(
                githubAccountRef,
                repoRef,
                null
        );
        PullRequestCollectContext  pullRequestCollectContext = new PullRequestCollectContext(
                githubAccountRef,
                repoRef,
                OffsetDateTime.parse("2019-01-01T00:00:00Z")
        );
        IssueCollectContext issueCollectContext = new IssueCollectContext(
                githubAccountRef,
                repoRef,
                OffsetDateTime.parse("2019-01-01T00:00:00Z")
        );

        // commit
        // 수집
        try{
            CollectResult<GithubCommitResponseDto, ShaCursor> commitResults =
                    commitCollector.collect(commitCollectContext);

            // 로그 출력
            log.info("[collect][commit] source={} fetched={}/{}(total) elapsed={}ms",
                    commitResults.source(),
                    commitResults.fetchedCount(),
                    commitResults.totalCount(),
                    commitResults.elapsedTimeMs()
            );
            // update
            try {
                commitUpsertService.upsertSingleTargetFromResponses(repoRef.repoId(), githubAccountRef.githubId(), commitResults.results());
                log.info("[upsert][commit] success");
            } catch (Exception e){
                log.error("[upsert][commit] failed", e);
            }
        } catch (Exception e){
            log.error("[collect][commit] failed", e);
        }

        // pull request
        try {
            // collect
            CollectResult<GithubPullRequestResponseDto, TimeCursor> prResults =
                    pullRequestCollector.collect(pullRequestCollectContext);

            log.info("[collect][pr] source={} fetched={}/{}(total) elapsed={}ms",
                    prResults.source(),
                    prResults.fetchedCount(),
                    prResults.totalCount(),
                    prResults.elapsedTimeMs()
            );
            // update
            try {
                pullRequestUpsertService.upsertSingleTargetFromResponses(repoRef.repoId(), githubAccountRef.githubId(), prResults.results());
                log.info("[upsert][pr] success");
            } catch (Exception e) {
                log.error("[upsert][pr] failed", e);
            }
        } catch (Exception e) {
            log.error("[collect][pr] failed", e);
        }

        // issue
        try {
            // collect
            CollectResult<GithubIssueResponseDto, TimeCursor> issueResults =
                    issueCollector.collect(issueCollectContext);

            log.info("[collect][issue] source={} fetched={}/{}(total) elapsed={}ms",
                    issueResults.source(),
                    issueResults.fetchedCount(),
                    issueResults.totalCount(),
                    issueResults.elapsedTimeMs()
            );
            // update
            try {
                issueUpsertService.upsertSingleTargetFromResponses(repoRef.repoId(), githubAccountRef.githubId(), issueResults.results());
                log.info("[upsert][issue] success");
            } catch (Exception e) {
                log.error("[upsert][issue] failed", e);
            }
        } catch (Exception e) {
            log.error("[collect][issue] failed", e);
        }

        log.info("< End collection for repository: {}", repoRef.fullName());

    }
}
