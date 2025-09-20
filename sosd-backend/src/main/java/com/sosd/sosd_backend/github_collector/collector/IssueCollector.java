package com.sosd.sosd_backend.github_collector.collector;

import com.sosd.sosd_backend.github_collector.api.GithubGraphQLClient;
import com.sosd.sosd_backend.github_collector.dto.collect.context.IssueCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.TimeCursor;
import com.sosd.sosd_backend.github_collector.dto.response.GithubIssueResponseDto;
import com.sosd.sosd_backend.github_collector.dto.response.GithubPullRequestResponseDto;
import com.sosd.sosd_backend.github_collector.dto.response.graphql.GithubIssueGraphQLResult;
import com.sosd.sosd_backend.github_collector.dto.response.graphql.GithubPageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IssueCollector implements GithubResourceCollector
<IssueCollectContext, GithubIssueResponseDto, TimeCursor>{

    private final GithubGraphQLClient githubGraphQLClient;

    private static final String QUERY = """
        query PRsByAuthorAndDate($q: String!, $first: Int = 50, $after: String) {
          search(query: $q, type: ISSUE, first: $first, after: $after) {
            issueCount
            pageInfo { hasNextPage hasPreviousPage startCursor endCursor }
            nodes {
              ... on Issue {
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
    public CollectResult<GithubIssueResponseDto, TimeCursor> collect(IssueCollectContext ctx) {
        // 0. 시간 측정 시작
        long startedNs = System.nanoTime();

        // 1. Context에서 필요한 정보 추출
        final String repoFullName = ctx.repoRef().fullName();               // e.g. "SKKU-OSP/spring-backend"
        final String author = ctx.githubAccountRef().githubLoginUsername(); // e.g. "byungKHee"
        final OffsetDateTime since = ctx.lastIssueDate() == null
                ? OffsetDateTime.parse("2018-12-31T23:59:59Z")
                : ctx.lastIssueDate();                   // 시작 시간, null이면 2019년부터 수집

        // 2. GraphQL variables 설정
        String q = "repo:%s is:issue author:%s created:>%s".formatted(repoFullName,author,since.toInstant().toString());
        int pageSize = 100;
        String after = null;
        OffsetDateTime lastCreatedAt = since; // update 후보

        // 3. 페이지 처리하며 issue 수집
        List<GithubIssueResponseDto> issueResults = new ArrayList<>();
        while (true) {

            // 3-1. GraphQL 쿼리 변수 설정
            Map<String, Object> vars = new HashMap<>();
            vars.put("q", q);
            vars.put("first", pageSize);
            if (after != null) vars.put("after", after);

            // 3-2. GraphQL 쿼리 실행
            var res = githubGraphQLClient.query(QUERY)
                    .variables(vars)
                    .executeWithAutoRotate(GithubIssueGraphQLResult.class);

            // 3-3. 에러 처리 및 결과 핸들링
            if (res.getErrors() != null && !res.getErrors().isEmpty()) {
                System.err.println("GraphQL Errors: " + res.getErrors());
                break;
            }
            GithubIssueGraphQLResult result = res.getData();
            if (result.search() == null || result.search().nodes() == null || result.search().nodes().isEmpty()) break;
            List<GithubIssueResponseDto> pageItems = result.search().nodes(); // issue 리스트
            GithubPageInfo pageInfo = result.search().pageInfo();             // 페이지 정보

            // 3-4. 수집한 issue 저장
            issueResults.addAll(pageItems);

            // 3-5. 최근순 정렬이므로, 첫번째 페이지에서 가장 최근 createdAt 갱신
            if (!pageInfo.hasPreviousPage()){
                for (var issue : pageItems) {
                    if (issue.createdAt().isAfter(lastCreatedAt)) {
                        lastCreatedAt = issue.createdAt();
                    }
                }
            }

            // 3-6. 종료 조건 검사
            if (!pageInfo.hasNextPage()) break;
            after = pageInfo.endCursor();
            if (after == null) break; // 방어적 탈출
        }

        // 4. 결과값 구성
        TimeCursor newCursor = new TimeCursor(lastCreatedAt);
        long elapsedTimeMs = Math.round((System.nanoTime() - startedNs) / 1_000_000.0);
        int fetchedCount = issueResults.size();

        // 5. 결과 반환
        return new CollectResult<>(
                issueResults,
                newCursor,
                fetchedCount,
                fetchedCount,
                elapsedTimeMs,
                source());
    }

    @Override
    public String source() {
        return "issue";
    }
}
