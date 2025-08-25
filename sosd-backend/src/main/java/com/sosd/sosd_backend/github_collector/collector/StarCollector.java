package com.sosd.sosd_backend.github_collector.collector;

import com.sosd.sosd_backend.github_collector.api.GithubGraphQLClient;
import com.sosd.sosd_backend.github_collector.dto.collect.context.StarCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.TimeCursor;
import com.sosd.sosd_backend.github_collector.dto.response.GithubStarResponseDto;
import com.sosd.sosd_backend.github_collector.dto.response.graphql.GithubPageInfo;
import com.sosd.sosd_backend.github_collector.dto.response.graphql.GithubStarGraphQLResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StarCollector implements GithubResourceCollector
<StarCollectContext, GithubStarResponseDto, TimeCursor> {

    private final GithubGraphQLClient githubGraphQLClient;

    private static final String QUERY = """
        query RepoStars(
          $owner: String!,
          $name: String!,
          $first: Int = 50,
          $after: String
        ) {
          repository(owner: $owner, name: $name) {
            stargazers(
              first: $first,
              after: $after,
              orderBy: { field: STARRED_AT, direction: DESC }
            ) {
              pageInfo { hasNextPage hasPreviousPage startCursor endCursor }
              edges {
                starredAt
                node {
                  databaseId
                }
              }
            }
          }
            rateLimit { cost used remaining limit resetAt }
        }
        """;

    @Override
    public CollectResult<GithubStarResponseDto, TimeCursor> collect(StarCollectContext ctx) {
        long startedNs = System.nanoTime();

        final String owner = ctx.repoRef().ownerName();               // e.g. "SKKU-OSP"
        final String name = ctx.repoRef().repoName();                 // e.g. "spring-back
        final OffsetDateTime since = ctx.lastStarredAt() == null
                ? OffsetDateTime.parse("2018-12-31T23:59:59Z")
                : ctx.lastStarredAt();                   // 시작 시간, null이면 2019년부터 수집
        int pageSize = 100;

        List<GithubStarResponseDto> starResults = new java.util.ArrayList<>();
        String after = null;
        OffsetDateTime lastCollectedStarredAt = since; // update 후보

        while (true) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", owner);
            vars.put("name", name);
            vars.put("first", pageSize);
            if (after != null) vars.put("after", after);
            var res = githubGraphQLClient.query(QUERY)
                    .variables(vars)
                    .execute(GithubStarGraphQLResult.class);

            GithubStarGraphQLResult result = res.getData();

            if (result.isInvalidStargazer()) break;

            // stargazer 리스트
            List<GithubStarResponseDto> pageItems = result.getStars();
            // 페이지 정보
            GithubPageInfo pageInfo = result.getPageInfo();

            // 가장 최신 starredAt 갱신 - 첫 페이지일 때
            if (!pageInfo.hasPreviousPage()) lastCollectedStarredAt = pageItems.getFirst().starredAt();

            // 최신부터 since까지 증분 수집
            boolean reachedSince = false;
            for (var star : pageItems) {
                if (star.starredAt().isBefore(since) || star.starredAt().isEqual(since)) {
                    reachedSince = true;
                    break;
                }
                starResults.add(star);
            }

            if (reachedSince || !pageInfo.hasNextPage()) break;
            after = pageInfo.endCursor();
        }

        TimeCursor newCursor = new TimeCursor(lastCollectedStarredAt);
        long elapsedTimeMs = Math.round((System.nanoTime() - startedNs) / 1_000_000.0);
        int fetchedCount = starResults.size();

        return new CollectResult<>(
                starResults,
                newCursor,
                fetchedCount,
                fetchedCount, // totalCount는 불명확
                elapsedTimeMs,
                source()
        );
    }

    @Override
    public String source() { return "star"; }

}
