package com.sosd.sosd_backend.github_collector;

import com.sosd.sosd_backend.github_collector.collector.IssueCollector;
import com.sosd.sosd_backend.github_collector.dto.collect.context.IssueCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.TimeCursor;
import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;
import com.sosd.sosd_backend.github_collector.dto.response.GithubIssueResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@SpringBootTest
public class IssueCollectTest {

    @Autowired
    private IssueCollector IssueCollector;

    @Test
    void testCollectIssues() {
        // ✅ 테스트용 Context (실제 구현에 맞게 RepoRef, GithubAccountRef 대체 필요)
        RepoRef testRepoRef = new RepoRef(
                123L,
                123L,
                "SKKU-SUCPI",
                "frontend-2.0",
                "SKKU-SUCPI/frontend-2.0",
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

        IssueCollectContext ctx = new IssueCollectContext(
                testAccountRef,
                testRepoRef,
                OffsetDateTime.parse("2025-05-01T00:00:00Z")
        );

        CollectResult<GithubIssueResponseDto, TimeCursor> result =
                IssueCollector.collect(ctx);

        System.out.println("=== Collect Result ===");
        System.out.println("Fetched count: " + result.fetchedCount());
        System.out.println("Total count: " + result.totalCount());
        System.out.println("Elapsed time (ms): " + result.elapsedTimeMs());
        System.out.println("Next cursor: " + result.cursor().lastCollectedTime());

        List<GithubIssueResponseDto> issues = result.results();
        for (GithubIssueResponseDto issue : issues) {
            System.out.printf("Issue #%d %s (createdAt=%s) : %d%n",
                    issue.number(), issue.title(), issue.createdAt(), issue.databaseId());
        }
    }
}

