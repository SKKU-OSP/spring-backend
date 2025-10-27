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

        // 0. 시간 측정 시작
        long startedNs = System.nanoTime();

        // 1. Context에서 필요한 정보 추출 & GraphQL variables 설정
        final String owner = ctx.repoRef().ownerName();               // e.g. "SKKU-OSP"
        final String name = ctx.repoRef().repoName();                 // e.g. "spring-back
        final OffsetDateTime since = ctx.lastStarredAt() == null
                ? OffsetDateTime.parse("2018-12-31T23:59:59Z")
                : ctx.lastStarredAt();                   // 시작 시간, null이면 2019년부터 수집
        int pageSize = 100;
        String after = null;
        OffsetDateTime lastCollectedStarredAt = since; // update 후보

        // 2. 페이지 처리하며 star 수집
        List<GithubStarResponseDto> starResults = new java.util.ArrayList<>();
        while (true) {

            // 2-1. GraphQL 쿼리 변수 설정
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", owner);
            vars.put("name", name);
            vars.put("first", pageSize);
            if (after != null) vars.put("after", after);

            // 2-2. GraphQL 쿼리 실행
            var res = githubGraphQLClient.query(QUERY)
                    .variables(vars)
                    .executeWithAutoRotate(GithubStarGraphQLResult.class);

            // 2-3. 에러 처리 및 결과 핸들링
            if (res.getErrors() != null && !res.getErrors().isEmpty()) {
                System.err.println("GraphQL Errors: " + res.getErrors());
                break;
            }
            GithubStarGraphQLResult result = res.getData();
            if (result.isInvalidStargazer()) break;
            List<GithubStarResponseDto> pageItems = result.getStars(); // star 리스트
            GithubPageInfo pageInfo = result.getPageInfo();            // 페이지 정보

            // 2-4. 첫번째 페이지에서 가장 최신 starredAt 갱신
            if (!pageInfo.hasPreviousPage()) lastCollectedStarredAt = pageItems.getFirst().starredAt();

            // 2-5. 마지막 수집 starredAt 전까지 순회하며 저장
            boolean reachedSince = false;
            for (var star : pageItems) {
                if (star.starredAt().isBefore(since) || star.starredAt().isEqual(since)) {
                    reachedSince = true;
                    break;
                }
                starResults.add(star);
            }

            // 2-6 종료 조건 검사
            if (reachedSince || !pageInfo.hasNextPage()) break;
            after = pageInfo.endCursor();
            if (after == null) break; // 방어적 탈출
        }

        // 3. 결과값 구성
        TimeCursor newCursor = new TimeCursor(lastCollectedStarredAt);
        long elapsedTimeMs = Math.round((System.nanoTime() - startedNs) / 1_000_000.0);
        int fetchedCount = starResults.size();

        // 4. 결과 반환
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
