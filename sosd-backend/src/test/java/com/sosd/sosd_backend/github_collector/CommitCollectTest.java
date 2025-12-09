package com.sosd.sosd_backend.github_collector;

import com.sosd.sosd_backend.github_collector.collector.CommitCollector;
import com.sosd.sosd_backend.github_collector.dto.collect.context.CommitCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.ShaCursor;
import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;
import com.sosd.sosd_backend.github_collector.dto.response.GithubCommitResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class CommitCollectTest {

    @Autowired
    private CommitCollector commitCollector;

    @Test
    void testCollectCommits() {
        RepoRef testRepoRef = new RepoRef(
                123L,
                123L,
                "SKKU-OSP",
                "spring-backend",
                "SKKU-OSP/spring-backend",
                LocalDateTime.now(),
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

        CommitCollectContext ctx = new CommitCollectContext(
                testAccountRef,
                testRepoRef,
                "cda95d8133a898ae12777e086ebc1f6d61805cb9"
        );

        CollectResult<GithubCommitResponseDto, ShaCursor> result =
                commitCollector.collect(ctx);

        System.out.println("=== Collect Result ===");
        System.out.println("Fetched count: " + result.fetchedCount());
        System.out.println("Total count: " + result.totalCount());
        System.out.println("Elapsed time (ms): " + result.elapsedTimeMs());
        System.out.println("Next cursor (last SHA): " + result.cursor().sha());

        for (GithubCommitResponseDto commit : result.results()) {
            System.out.printf("Commit %s: %s (authoredDate=%s) additions:%d deletions:%d%n",
                    commit.sha(), commit.message(), commit.authoredDate(), commit.additions(), commit.deletions());
        }

    }

}
