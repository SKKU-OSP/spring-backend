package com.sosd.sosd_backend.github_collector;

import com.sosd.sosd_backend.github_collector.collector.PullRequestCollector;
import com.sosd.sosd_backend.github_collector.dto.collect.context.PullRequestCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.TimeCursor;
import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;
import com.sosd.sosd_backend.github_collector.dto.response.GithubPullRequestResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@SpringBootTest
class PRCollectTest {

    @Autowired
    private PullRequestCollector pullRequestCollector;

    @Test
    void testCollectPullRequests() {
        // ✅ 테스트용 Context (실제 구현에 맞게 RepoRef, GithubAccountRef 대체 필요)
        RepoRef testRepoRef = new RepoRef(
                123L,
                123L,
                "SKKU-OSP",
                "spring-backend",
                "SKKU-OSP/spring-backend",
                LocalDateTime.now()
        );

        GithubAccountRef testAccountRef = new GithubAccountRef(
                80045655L,
                "MDQ6VXNlcjgwMDQ1NjU1",
                "byungKHee",
                null,
                null,
                null,
                null
        );

        PullRequestCollectContext ctx = new PullRequestCollectContext(
                testAccountRef,
                testRepoRef,
                OffsetDateTime.parse("2025-08-01T00:00:00Z")
        );

        CollectResult<GithubPullRequestResponseDto, TimeCursor> result =
                pullRequestCollector.collect(ctx);

        System.out.println("=== Collect Result ===");
        System.out.println("Fetched count: " + result.fetchedCount());
        System.out.println("Total count: " + result.totalCount());
        System.out.println("Elapsed time (ms): " + result.elapsedTimeMs());
        System.out.println("Next cursor: " + result.cursor().lastCollectedTime());

        List<GithubPullRequestResponseDto> prs = result.results();
        for (GithubPullRequestResponseDto pr : prs) {
            System.out.printf("PR #%d [%s] %s (createdAt=%s) : %d%n",
                    pr.number(), pr.state(), pr.title(), pr.createdAt(), pr.databaseId());
        }
    }
}