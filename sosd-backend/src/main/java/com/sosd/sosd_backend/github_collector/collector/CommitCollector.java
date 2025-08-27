package com.sosd.sosd_backend.github_collector.collector;

import com.sosd.sosd_backend.github_collector.api.GithubGraphQLClient;
import com.sosd.sosd_backend.github_collector.dto.collect.context.CommitCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.ShaCursor;
import com.sosd.sosd_backend.github_collector.dto.response.GithubCommitResponseDto;
import com.sosd.sosd_backend.github_collector.dto.response.graphql.GithubCommitGraphQLResult;
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
public class CommitCollector implements GithubResourceCollector
<CommitCollectContext, GithubCommitResponseDto, ShaCursor> {

    private final GithubGraphQLClient githubGraphQLClient;

    private static final String QUERY = """
        query RepoCommitsByAuthor(
          $owner: String!,
          $name: String!,
          $authorId: ID!,
          $first: Int = 100,
          $after: String,
          $since: GitTimestamp
        ) {
          repository(owner: $owner, name: $name) {
            defaultBranchRef {
              name
              target {
                ... on Commit {
                  oid   # HEAD SHA (새 lastSha로 저장하기 좋음)
                  history(
                    first: $first,
                    after: $after,
                    author: { id: $authorId },
                    since: $since,
                  ) {
                    pageInfo { hasNextPage endCursor }
                    totalCount
                    nodes {
                      oid
                      messageHeadline
                      authoredDate
                      committedDate
                      additions
                      deletions
                      author {
                        name
                        user {login id}
                      }
                    }
                  }
                }
              }
            }
          }
          rateLimit { cost used remaining limit resetAt }
        }
        """;

    @Override
    public CollectResult<GithubCommitResponseDto, ShaCursor> collect(CommitCollectContext ctx) {
        if (ctx.lastCommitSha() == null) {
            // 이전 수집 기록이 없는 경우 전체 수집
            return collectAll(ctx);
        } else {
            // 이전 수집 기록이 있는 경우 HEAD부터 마지막 수집 커밋 sha전까지 수집
            return collectHeadToLastCommit(ctx);
        }
    }


    /**
     * DB에 이전 수집 기록이 없는 경우 전체 수집
     * @param ctx
     * @return
     */
    private CollectResult<GithubCommitResponseDto, ShaCursor> collectAll(CommitCollectContext ctx) {

        // 0. 시간 측정 시작
        long startedNs = System.nanoTime();

        // 1. Context에서 필요한 정보 추출 & GraphQL variables 설정
        final String owner = ctx.repoRef().ownerName();
        final String name = ctx.repoRef().repoName();
        final String authorId = ctx.githubAccountRef().githubGraphqlNodeId();
        final String since = OffsetDateTime.parse("2019-01-01T00:00:00Z") // 서비스 시작일인 2019년부터 수집
                .toInstant()
                .toString();
        int pageSize = 100;
        String after = null;
        String updateLastSha = null; // HEAD SHA

        // 2. 페이지 처리하며 commit 수집
        List<GithubCommitResponseDto> commitResults = new ArrayList<>();
        while (true) {

            // 2-1. GraphQL 쿼리 변수 설정
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", owner);
            vars.put("name", name);
            vars.put("authorId", authorId);
            vars.put("first", pageSize);
            vars.put("since", since);
            if (after != null) vars.put("after", after);

            // 2-2. GraphQL 쿼리 실행
            var res = githubGraphQLClient.query(QUERY)
                    .variables(vars)
                    .execute(GithubCommitGraphQLResult.class);

            // 2-3. 에러 처리 및 결과 핸들링
            if (res.getErrors() != null && !res.getErrors().isEmpty()) {
                System.err.println("GraphQL Errors: " + res.getErrors());
                break;
            }
            GithubCommitGraphQLResult result = res.getData();
            if (result.isInvalidCommits()) break;
            List<GithubCommitResponseDto> pageItems = result.commitsOrEmpty(); // commit 리스트
            GithubPageInfo pageInfo = result.pageInfo();                       // 페이지 정보

            // 2-4. 수집한 commit 저장
            commitResults.addAll(pageItems);

            // 2-5. 첫번째 페이지에서 가장 최신 커밋 SHA 갱신
            if (!pageInfo.hasPreviousPage()) updateLastSha = result.headSha();

            // 2-6. 종료 조건 검사
            if (!pageInfo.hasNextPage()) break;
            after = pageInfo.endCursor();
            if (after == null) break; // 방어적 탈출
        }

        // 3. 결과값 구성
        ShaCursor newCursor = new ShaCursor(updateLastSha);
        long elapsedTimeMs = Math.round((System.nanoTime() - startedNs) / 1_000_000.0);
        int fetchedCount = commitResults.size();

        // 4. 결과 반환
        return new CollectResult<>(
                commitResults,
                newCursor,
                fetchedCount,
                fetchedCount,
                elapsedTimeMs,
                source());
    }

    /**
     * DB에 이전 수집 기록이 있는 경우 HEAD부터 마지막 수집 커밋 sha전까지 수집
     * @param ctx
     * @return
     */
    private CollectResult<GithubCommitResponseDto, ShaCursor> collectHeadToLastCommit(CommitCollectContext ctx) {

        // 0. 시간 측정 시작
        long startedNs = System.nanoTime();

        // 1. Context에서 필요한 정보 추출 & GraphQL variables 설정
        final String owner = ctx.repoRef().ownerName();
        final String name = ctx.repoRef().repoName();
        final String authorId = ctx.githubAccountRef().githubGraphqlNodeId();
        final String lastSha = ctx.lastCommitSha();
        final String since = OffsetDateTime.now() // lastSha가 삭제된 경우를 대비해 1년의 여유 마진
                .minusYears(1)
                .toInstant()
                .toString();
        int pageSize = 30; // 증분형 수집은 페이지 크기를 줄여서 자주 수집
        String after = null;
        String updateLastSha = lastSha; // HEAD SHA 후보

        // 2. 페이지 처리하며 commit 수집
        List<GithubCommitResponseDto> commitResults = new ArrayList<>();
        while (true) {

            // 2-1. GraphQL 쿼리 변수 설정
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", owner);
            vars.put("name", name);
            vars.put("authorId", authorId);
            vars.put("first", pageSize);
            vars.put("since", since);
            if (after != null) vars.put("after", after);

            // 2-2. GraphQL 쿼리 실행
            var res = githubGraphQLClient.query(QUERY)
                    .variables(vars)
                    .execute(GithubCommitGraphQLResult.class);

            // 2-3. 에러 처리 및 결과 핸들링
            if (res.getErrors() != null && !res.getErrors().isEmpty()) {
                System.err.println("GraphQL Errors: " + res.getErrors());
                break;
            }
            GithubCommitGraphQLResult result = res.getData();
            if (result.isInvalidCommits()) break;
            List<GithubCommitResponseDto> pageItems = result.commitsOrEmpty(); // commit 리스트
            GithubPageInfo pageInfo = result.pageInfo();                       // 페이지 정보

            // 2-4. 첫번째 페이지에서 가장 최신 커밋 SHA 갱신
            if (!pageInfo.hasPreviousPage()) updateLastSha = result.headSha();

            // 2-5. 마지막 수집 커밋 sha 전까지 순회하며 저장
            boolean foundLastSha = false;
            for (var commit : pageItems) {
                if (commit.sha().equals(lastSha)){
                    foundLastSha = true;
                    break;
                }
                commitResults.add(commit);
            }

            // 2-6. 종료 조건 검사
            if (foundLastSha || !pageInfo.hasNextPage()) break;
            after = pageInfo.endCursor();
            if (after == null) break; // 방어적 탈출
        }

        // 3. 결과값 구성
        ShaCursor newCursor = new ShaCursor(updateLastSha);
        long elapsedTimeMs = Math.round((System.nanoTime() - startedNs) / 1_000_000.0);
        int fetchedCount = commitResults.size();

        // 4. 결과 반환
        return new CollectResult<>(
                commitResults,
                newCursor,
                fetchedCount,
                fetchedCount,
                elapsedTimeMs,
                source());
    }

    @Override
    public String source() { return "commit"; }
}
