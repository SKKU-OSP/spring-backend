package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.dto.api.PrCommitListApiDto;
import com.sosd.sosd_backend.github_collector.api.GithubGraphQLClient;
import com.sosd.sosd_backend.github_collector.dto.response.graphql.GithubPrCommitListGraphQLResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** PR의 커밋 메타데이터만 GraphQL로 페이지네이션해 조회한다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestCommitService {

    private static final int PAGE_SIZE = 100;
    private static final String QUERY = """
        query PullRequestCommits(
          $owner: String!, $repo: String!, $number: Int!, $first: Int!, $after: String
        ) {
          repository(owner: $owner, name: $repo) {
            pullRequest(number: $number) {
              commits(first: $first, after: $after) {
                totalCount
                pageInfo { hasNextPage hasPreviousPage startCursor endCursor }
                nodes {
                  commit {
                    oid
                    messageHeadline
                    changedFilesIfAvailable
                    additions
                    deletions
                  }
                }
              }
            }
          }
          rateLimit { cost used remaining limit resetAt }
        }
        """;

    private final GithubGraphQLClient graphQLClient;

    public PrCommitListApiDto getCommits(String owner, String repo, int prNumber) {
        List<PrCommitListApiDto.CommitSummary> commits = new ArrayList<>();
        String after = null;
        int totalCount = 0;

        while (true) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("owner", owner);
            variables.put("repo", repo);
            variables.put("number", prNumber);
            variables.put("first", PAGE_SIZE);
            if (after != null) variables.put("after", after);

            var response = graphQLClient.query(QUERY)
                    .variables(variables)
                    .executeWithAutoRotate(GithubPrCommitListGraphQLResult.class);

            if (response.getErrors() != null && !response.getErrors().isEmpty()) {
                throw new IllegalStateException("GitHub PR 커밋 조회 실패: " + response.getErrors());
            }
            var data = response.getData();
            if (data == null || data.repository() == null
                    || data.repository().pullRequest() == null) {
                throw new IllegalArgumentException(
                        "PR을 찾을 수 없습니다: " + owner + "/" + repo + "#" + prNumber);
            }

            var connection = data.repository().pullRequest().commits();
            if (connection == null) break;
            totalCount = connection.totalCount();
            if (connection.nodes() != null) {
                connection.nodes().stream()
                        .filter(node -> node != null && node.commit() != null)
                        .map(node -> node.commit())
                        .map(commit -> PrCommitListApiDto.CommitSummary.builder()
                                .sha(commit.oid())
                                .messageHeadline(commit.messageHeadline())
                                .fileCount(commit.changedFilesIfAvailable())
                                .additions(commit.additions())
                                .deletions(commit.deletions())
                                .build())
                        .forEach(commits::add);
            }

            var pageInfo = connection.pageInfo();
            if (pageInfo == null || !pageInfo.hasNextPage() || pageInfo.endCursor() == null) break;
            after = pageInfo.endCursor();
        }

        log.info("PR 커밋 목록 조회 완료: {}/{}#{} ({}개)", owner, repo, prNumber, commits.size());
        return PrCommitListApiDto.builder()
                .owner(owner)
                .repo(repo)
                .prNumber(prNumber)
                .totalCount(totalCount)
                .commits(commits)
                .build();
    }
}
