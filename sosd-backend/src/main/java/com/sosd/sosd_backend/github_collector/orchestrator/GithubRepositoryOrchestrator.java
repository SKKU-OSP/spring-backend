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
import com.sosd.sosd_backend.service.github.GithubAccountRepositoryLinkService;
import com.sosd.sosd_backend.service.github.IssueUpsertService;
import com.sosd.sosd_backend.service.github.PullRequestUpsertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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

    // link table
    private final GithubAccountRepositoryLinkService linkService;

    // util
    private static final OffsetDateTime DEFAULT_SINCE =
            OffsetDateTime.parse("2019-01-01T00:00:00Z");

    // OffsetDateTime(커서) -> DB 저장용 LocalDateTime(UTC)
    private static LocalDateTime toUtcLocal(OffsetDateTime odt) {
        return LocalDateTime.ofInstant(odt.toInstant(), ZoneOffset.UTC);
    }

    public void collectByRepository(GithubAccountRef githubAccountRef, RepoRef repoRef){
        // commit 수집
        collectCommits(githubAccountRef, repoRef);
        // pr 수집
        collectPullRequests(githubAccountRef, repoRef);
        // issue 수집
        collectIssues(githubAccountRef, repoRef);
    }

    private void collectCommits(GithubAccountRef acc, RepoRef repo) {
        try{
            // 1) 커서 가져오기
            String lastSha = linkService.getLastCommitSha(acc.githubId(), repo.repoId()).orElse(null);

            // 2) 수집
            CollectResult<GithubCommitResponseDto, ShaCursor> commitResults =
                    commitCollector.collect(new CommitCollectContext(acc, repo, lastSha));

            log.info("[collect][{}] source={} fetched={}/{}(total) elapsed={}ms",
                    repo.fullName(),
                    commitResults.source(),
                    commitResults.fetchedCount(),
                    commitResults.totalCount(),
                    commitResults.elapsedTimeMs()
            );

            // 3) 기여내역 upsert
            try{
                commitUpsertService.upsertSingleTargetFromResponses(
                        repo.repoId(),
                        acc.githubId(),
                        commitResults.results()
                );
                log.info("[upsert][{}] commits upsert success",
                        repo.fullName()
                );
            } catch (Exception e) {
                log.error("[upsert][{}] commit upsert failed", repo.fullName(), e);
                return; // 오류나면 커서 업데이트 x
            }

            // 4) 커서 update
            // 4-1) 업데이트 유무 확인
            if (commitResults.cursor() == null || commitResults.fetchedCount() == 0) return;

            // 4-2) update
            try {
                linkService.updateCommitCursor(acc.githubId(), repo.repoId(),commitResults.cursor().sha());
            } catch (Exception e) {
                log.error("[update][{}] commit cursor update failed", repo.fullName(), e);
            }

        } catch (Exception e) {
            log.error("[collect][{}] commit collect failed", repo.fullName(), e);
        }
    }

    private void collectPullRequests(GithubAccountRef acc, RepoRef repo) {
        try{
            // 1) 커서 가져오기
            OffsetDateTime since = linkService.getLastPrDate(acc.githubId(), repo.repoId())
                    .map(dt -> dt.atOffset(ZoneOffset.UTC))
                    .orElse(DEFAULT_SINCE);

            // 2) 수집
            CollectResult<GithubPullRequestResponseDto, TimeCursor> pullRequestResults =
                    pullRequestCollector.collect(new PullRequestCollectContext(acc, repo, since));

            log.info("[collect][{}] source={} fetched={}/{}(total) elapsed={}ms",
                    repo.fullName(),
                    pullRequestResults.source(),
                    pullRequestResults.fetchedCount(),
                    pullRequestResults.totalCount(),
                    pullRequestResults.elapsedTimeMs()
            );

            // 3) upsert
            try{
                pullRequestUpsertService.upsertSingleTargetFromResponses(
                        repo.repoId(),
                        acc.githubId(),
                        pullRequestResults.results()
                );
                log.info("[upsert][{}] prs upsert success", repo.fullName());
            } catch (Exception e) {
                log.error("[upsert][{}] pr upsert failed", repo.fullName(), e);
                return; // 오류나면 커서 업데이트 x
            }

            // 4) 커서 update
            // 4-1) 업데이트 유무 확인
            if  (pullRequestResults.cursor() == null || pullRequestResults.fetchedCount() == 0) return;

            // 4-2) update
            try {
                linkService.updatePrCursor(
                        acc.githubId(),
                        repo.repoId(),
                        toUtcLocal(pullRequestResults.cursor().lastCollectedTime())
                );
            } catch (Exception e) {
                log.error("[update][{}] pr cursor update failed", repo.fullName(), e);
            }

        } catch (Exception e) {
            log.error("[collect][{}] pr collect failed", repo.fullName(), e);
        }

    }

    private void collectIssues(GithubAccountRef acc, RepoRef repo) {
        try{
            // 1) 커서 가져오기
            OffsetDateTime since = linkService.getLastIssueDate(acc.githubId(), repo.repoId())
                    .map(dt -> dt.atOffset(ZoneOffset.UTC))
                    .orElse(DEFAULT_SINCE);

            // 2) 수집
            CollectResult<GithubIssueResponseDto, TimeCursor> issueResults =
                    issueCollector.collect(new IssueCollectContext(acc, repo, since));

            log.info("[collect][{}] source={} fetched={}/{}(total) elapsed={}ms",
                    repo.fullName(),
                    issueResults.source(),
                    issueResults.fetchedCount(),
                    issueResults.totalCount(),
                    issueResults.elapsedTimeMs()
            );

            // 3) upsert
            try{
                issueUpsertService.upsertSingleTargetFromResponses(
                        repo.repoId(),
                        acc.githubId(),
                        issueResults.results()
                );
                log.info("[upsert][{}] issues upsert success", repo.fullName());
            } catch (Exception e) {
                log.error("[upsert][{}] issue upsert failed", repo.fullName(), e);
                return; // 오류나면 커서 업데이트 x
            }

            // 4) 커서 update
            // 4-1) 업데이트 유무 확인
            if (issueResults.cursor() == null || issueResults.fetchedCount() == 0) return;

            // 4-2) update
            try {
                linkService.updateIssueCursor(
                        acc.githubId(),
                        repo.repoId(),
                        toUtcLocal(issueResults.cursor().lastCollectedTime())
                );
            } catch (Exception e) {
                log.error("[update][{}] issue cursor update failed", repo.fullName(), e);
            }

        } catch (Exception e) {
            log.error("[collect][{}] issue collect failed", repo.fullName(), e);
        }

    }

}
