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
                id
                number
                title
                body
                createdAt
              }
            }
          }
          rateLimit { cost used remaining limit resetAt }
        }
        """;

    @Override
    public CollectResult<GithubIssueResponseDto, TimeCursor> collect(IssueCollectContext ctx) {
        long startedNs = System.nanoTime();

        final String repoFullName = ctx.repoRef().fullName();               // e.g. "SKKU-OSP/spring-backend"
        final String author = ctx.githubAccountRef().githubLoginUsername(); // e.g. "byungKHee"
        final OffsetDateTime since = ctx.lastIssueDate();                   // 시작 시간

        String q = "repo:%s is:issue author:%s created:>%s".formatted(repoFullName,author,since.toString());
        int pageSize = 100;

        List<GithubIssueResponseDto> issueResults = new ArrayList<>();
        String after = null;
        OffsetDateTime lastCreatedAt = since; // update 후보

        while (true) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("q", q);
            vars.put("first", pageSize);
            if (after != null) vars.put("after", after);

            var res = githubGraphQLClient.query(QUERY)
                    .variables(vars)
                    .execute(GithubIssueGraphQLResult.class);

            GithubIssueGraphQLResult result = res.getData();
            if (result.search() == null || result.search().nodes() == null || result.search().nodes().isEmpty()) break;

            // issue 리스트
            List<GithubIssueResponseDto> pageItems = result.search().nodes();
            // 페이지 정보
            GithubPageInfo pageInfo = result.search().pageInfo();

            issueResults.addAll(pageItems);

            // 마지막 페이지
            if (!pageInfo.hasNextPage()){
                // 마지막 issue 갱신
                for (var issue : pageItems) {
                    if (issue.createdAt().isAfter(lastCreatedAt)) lastCreatedAt = issue.createdAt();
                    break;
                }
            }
            after = pageInfo.endCursor();
        }

        TimeCursor newCursor = new TimeCursor(lastCreatedAt);

        long elapsedTimeMs = Math.round((System.nanoTime() - startedNs) / 1_000_000.0);
        int fetchedCount = issueResults.size();

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
