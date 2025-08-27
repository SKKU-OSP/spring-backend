package com.sosd.sosd_backend.github_collector;

import com.sosd.sosd_backend.github_collector.dto.collect.context.RepoListCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.TimeCursor;
import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.response.GithubRepositoryResponseDto;
import com.sosd.sosd_backend.github_collector.collector.RepoCollector;
import com.sosd.sosd_backend.github_collector.dto.response.graphql.GithubRepositoryGraphQLResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RepoCollectTest {

    @Autowired
    private RepoCollector repoCollector;
    @Test
    void testGetAllReposFromUser(){
        GithubAccountRef testAccountRef = new GithubAccountRef(
                80045655L,
                "MDQ6VXNlcjgwMDQ1NjU1",
                "byungKHee",
                null,
                null,
                null
        );

        RepoListCollectContext ctx = new RepoListCollectContext(
                testAccountRef,
                OffsetDateTime.parse("2021-01-01T00:00:00Z")
        );

        CollectResult<GithubRepositoryResponseDto, TimeCursor> result =
                repoCollector.collect(ctx);

        System.out.println("=== Collect Result ===");
        System.out.println("Fetched count: " + result.fetchedCount());
        System.out.println("Total count: " + result.totalCount());
        System.out.println("Elapsed time (ms): " + result.elapsedTimeMs());
        System.out.println("Next cursor: " + result.cursor().lastCollectedTime());
        List<GithubRepositoryResponseDto> repos = result.results();
        for (GithubRepositoryResponseDto repo : repos) {
            System.out.printf("Repo #%d [%s] (createdAt=%s)%n",
                    repo.githubRepoId(),
                    repo.fullName(),
                    repo.githubRepositoryCreatedAt()
            );
        }

    }
}
