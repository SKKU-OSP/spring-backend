package com.sosd.sosd_backend.github_collector.collector;

import com.sosd.sosd_backend.github_collector.api.GithubGraphQLClient;
import com.sosd.sosd_backend.github_collector.dto.collect.context.PullRequestCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.TimeCursor;
import com.sosd.sosd_backend.github_collector.dto.response.GithubPullRequestResponseDto;
import com.sosd.sosd_backend.github_collector.dto.response.graphql.GithubPageInfo;
import com.sosd.sosd_backend.github_collector.dto.response.graphql.GithubPullRequestGraphQLResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PullRequestCollector implements GithubResourceCollector
<PullRequestCollectContext, GithubPullRequestResponseDto, TimeCursor>{

    private final GithubGraphQLClient graphQLClient;

    private static final String QUERY = """
        query PRsByAuthorAndDate($q: String!, $first: Int = 50, $after: String) {
          search(query: $q, type: ISSUE, first: $first, after: $after) {
            issueCount
            pageInfo { hasNextPage hasPreviousPage startCursor endCursor }
            nodes {
              ... on PullRequest {
                databaseId
                number
                title
                body
                state
                createdAt
              }
            }
          }
          rateLimit { cost used remaining limit resetAt }
        }
        """;

    @Override
    public CollectResult<GithubPullRequestResponseDto, TimeCursor> collect(PullRequestCollectContext ctx){

        // 0. 시간 측정 시작
        long startedNs = System.nanoTime(); // 시간 측정 시작

        // 1. Context에서 필요한 정보 추출
        final String repoFullName = ctx.repoRef().fullName();               // e.g. "SKKU-OSP/spring-backend"
        final String author = ctx.githubAccountRef().githubLoginUsername(); // e.g. "byungKHee"
        final OffsetDateTime since = ctx.lastPrDate() == null
                ? OffsetDateTime.parse("2018-12-31T23:59:59Z")
                : ctx.lastPrDate();                   // 시작 시간, null이면 2019년부터 수집

        // 2. GraphQL variables 설정
        String q = "repo:%s is:pr author:%s created:>%s".formatted(repoFullName,author,since.toInstant().toString());
        int pageSize = 100;
        String after = null;
        OffsetDateTime lastCreatedAt = since; // update 후보

        // 3. 페이징 처리하며 PR 수집
        List<GithubPullRequestResponseDto> pullRequests = new ArrayList<>();
        while (true){

            // 3-1. GraphQL 쿼리 변수 설정
            Map<String, Object> vars = new HashMap<>();
            vars.put("q", q);
            vars.put("first", pageSize);
            if (after != null) vars.put("after", after);

            // 3-2. GraphQL 쿼리 실행
            var res = graphQLClient.query(QUERY)
                    .variables(vars)
                    .execute(GithubPullRequestGraphQLResult.class);

            // 3-3. 에러 처리 및 결과 핸들링
            if (res.getErrors() != null && !res.getErrors().isEmpty()) {
                // 에러가 발생한 경우 로그 출력 후 종료
                System.err.println("GraphQL Errors: " + res.getErrors());
                break;
            }
            GithubPullRequestGraphQLResult result = res.getData();
            if (result.search() == null || result.search().nodes() == null || result.search().nodes().isEmpty()) break;
            List<GithubPullRequestResponseDto> pageItems = result.search().nodes(); // 현재 페이지의 PR 리스트
            GithubPageInfo pageInfo = result.search().pageInfo();                   // 페이지 정보

            // 3-4. 수집한 PR 저장
            pullRequests.addAll(pageItems);

            // 3-5. 최근순 정렬이므로, 첫번째 페이지에서 가장 최근 createdAt 갱신
            if (!pageInfo.hasPreviousPage()){
                for (var pr : pageItems) {
                    if (pr.createdAt() != null && pr.createdAt().isAfter(lastCreatedAt)) {
                        lastCreatedAt = pr.createdAt();
                    }
                }
            }

            // 3-6. 종료 조건 검사
            if (!pageInfo.hasNextPage()) break; // 마지막 페이지
            after = pageInfo.endCursor();
            if (after == null) break; // 방어적 탈출
        }

        // 4. 결과값 구성
        TimeCursor newCursor = new TimeCursor(lastCreatedAt);
        long elapsedTimeMs = Math.round((System.nanoTime() - startedNs) / 1_000_000.0); // 시간 측정 종료
        int fetchedCount = pullRequests.size();

        // 5. 결과 반환
        return new CollectResult<>(
                pullRequests,
                newCursor,
                fetchedCount, // fetchedCount
                fetchedCount, // fetchedCount
                elapsedTimeMs,
                source());
    }

    @Override
    public String source() { return "pr"; }
}
