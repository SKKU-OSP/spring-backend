package com.sosd.sosd_backend.github_collector;

import com.sosd.sosd_backend.github_collector.collector.StarCollector;
import com.sosd.sosd_backend.github_collector.dto.collect.context.StarCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.TimeCursor;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;
import com.sosd.sosd_backend.github_collector.dto.response.GithubStarResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@SpringBootTest
public class StarCollectTest {

    @Autowired
    private StarCollector starCollector;

    @Test
    void testCollectStars() {
        RepoRef testRepoRef = new RepoRef(
                123L,
                123L,
                "SKKU-OSP",
                "SKKU-OSP",
                "SKKU-OSP/SKKU-OSP",
                LocalDateTime.now()
        );

        StarCollectContext ctx = new StarCollectContext(
                testRepoRef,
                null
//                OffsetDateTime.parse("2024-10-11T07:47:16Z")
        );

        CollectResult<GithubStarResponseDto, TimeCursor> result =
                starCollector.collect(ctx);

        System.out.println("=== Collect Result ===");
        System.out.println("Fetched count: " + result.fetchedCount());
        System.out.println("Total count: " + result.totalCount());
        System.out.println("Elapsed time (ms): " + result.elapsedTimeMs());
        System.out.println("Next cursor (last starredAt): " + result.cursor().lastCollectedTime());
        for (GithubStarResponseDto star : result.results()) {
            System.out.printf("StarredAt: %s, UserID: %s%n",
                    star.starredAt(), star.node().id());
        }
    }
}
