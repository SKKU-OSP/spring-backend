package com.sosd.sosd_backend.github_collector.collector;

import com.sosd.sosd_backend.github_collector.api.GithubGraphQLClient;
import com.sosd.sosd_backend.github_collector.dto.collect.context.RepoListCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.TimeCursor;
import com.sosd.sosd_backend.github_collector.dto.response.GithubRepositoryResponseDto;
import com.sosd.sosd_backend.github_collector.dto.RepoCollectorDtos.EventRepoDto;
import com.sosd.sosd_backend.github_collector.dto.RepoCollectorDtos.SearchIssuesDto;
import com.sosd.sosd_backend.github_collector.dto.RepoCollectorDtos.UserRepoDto;
import com.sosd.sosd_backend.github_collector.api.GithubRestClient;
import com.sosd.sosd_backend.github_collector.dto.response.graphql.GithubRepositoryGraphQLResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.OffsetDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class RepoCollector implements GithubResourceCollector
<RepoListCollectContext, GithubRepositoryResponseDto, TimeCursor>{

    private final GithubRestClient githubRestClient;
    private final GithubGraphQLClient githubGraphQLClient;

    @Override
    public CollectResult<GithubRepositoryResponseDto, TimeCursor> collect(RepoListCollectContext context) {

        // 0. 시간 측정 시작
        long startedNs = System.nanoTime();
        TimeCursor newCursor = new TimeCursor(context.lastCrawling()); // 이전 크롤링 시점

        // 1. 기여한 레포 full name 목록 수집
        Set<String> fullNames = new HashSet<>();
        // 1-1. 사용자의 모든 공개 repo 목록에서 추출 - 수집기록이 없으면 실행
        if (context.lastCrawling() == null) {
            fullNames.addAll(fetchReposFromUserRepos(context.githubAccountRef().githubLoginUsername()));
        }
        // 1-2. 사용자가 최근에 기여한 이슈/PR에서 repo 추출 - 수집기록이 없거나 2일 이상 지난 경우 실행
        if (context.lastCrawling() == null || context.lastCrawling().isBefore(OffsetDateTime.now().minusDays(2))) {
            fullNames.addAll(fetchReposFromSearchIssues(context.githubAccountRef().githubLoginUsername(), context.lastCrawling()));
        }
        // 1-3. 사용자의 이벤트에서 기여한 repo 추출 - 항상 실행
        fullNames.addAll(fetchReposFromEvents(context.githubAccountRef().githubLoginUsername()));

        // 2. 각 repo의 상세 정보 수집
        List<GithubRepositoryGraphQLResult> results = new ArrayList<>();
        for (String fullName : fullNames) {
            String[] parts = fullName.split("/");
            if (parts.length == 2) {
                var res = getRepoInfoGraphQL(parts[0], parts[1]);
                if (res != null) {
                    results.add(res);
                }
            }
        }

        // 3. 결과값 구성
        List<GithubRepositoryResponseDto> repoList = results.stream()
                .map(GithubRepositoryResponseDto::from)
                .toList();
        newCursor = new TimeCursor(OffsetDateTime.now());
        long elapsedTimeMs = Math.round((System.nanoTime() - startedNs) / 1_000_000.0); // 시간 측정 종료
        int fetchedCount = repoList.size();

        // 4. 결과 반환
        return new CollectResult<>(
                repoList,
                newCursor,
                fetchedCount,
                fetchedCount,
                elapsedTimeMs,
                source()
        );
    }

    @Override
    public String source() { return "repo"; }

    /**
     * 단일 repo 정보 조회 - GraphQL
     */
    private GithubRepositoryGraphQLResult getRepoInfoGraphQL(String owner, String name) {
        final String query = """
                query RepoOverview($owner: String!, $name: String!) {
                  repository(owner: $owner, name: $name) {
                    databaseId
                    nameWithOwner                       # full_name 대체 가능
                    isPrivate                           # is_private
                    defaultBranchRef { name }           # default_branch
                    description                         # description
                    stargazerCount                      # star
                    watchers { totalCount }             # watcher (구독자 수)
                    forkCount                           # fork
                    licenseInfo { name }                # license
                    createdAt                           # github_repository_created_at
                    updatedAt                           # github_repository_updated_at
                    pushedAt                            # github_pushed_at
                
                    # README 내용 (루트의 README.md 기준)
                    object(expression: "HEAD:README.md") {
                      ... on Blob { text }              # readme
                    }
                
                    # 사용 언어 분포 (상위 5개)
                    languages(first: 5, orderBy: {field: SIZE, direction: DESC}) {
                      totalSize
                      edges {
                        size
                        node { name }
                      }
                    }
                    # Dependency
                    dependencyGraphManifests(first: 1) {
                      totalCount
                    }
                    # contributor 수 집계용 (협업자 수)
                    # 정확한 협업자 수와는 조금 다르기에 추후 직접 집계 필요
                    mentionableUsers(first: 0) { totalCount }
                  }
                  rateLimit { cost used remaining limit resetAt }
                }
                """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("owner", owner);
        variables.put("name", name);

        var res = githubGraphQLClient.query(query)
                .variables(variables)
                .execute(GithubRepositoryGraphQLResult.class);

        if (res.getErrors() != null && !res.getErrors().isEmpty()) {
            // 에러가 발생한 경우 로그 출력 후 null 반환
            System.err.println("GraphQL Errors: " + res.getErrors());
            return null;
        }
        return res.getData();
    }

    /**
     * 1. username의 모든 공개 repo의 full_name 목록 수집
     */
    private Set<String> fetchReposFromUserRepos(String username) {
        List<UserRepoDto> repos = githubRestClient.request()
                .endpoint("/users/" + username + "/repos")
                .queryParam("type", "all")
                .getList(new ParameterizedTypeReference<>() {});

        Set<String> result = new HashSet<>();
        repos.stream()
                .map(UserRepoDto::fullName)
                .forEach(result::add);
        return result;
    }

    /**
     * 2. username이 최근에 기여한 이슈/PR에서 repo의 full_name 추출
     */
    private Set<String> fetchReposFromSearchIssues(String username, OffsetDateTime since) {
        record SearchIssuesResponse(List<SearchIssuesDto> items) {}

        // 쿼리 파라미터 설정
        int page = 1;
        int perPage = 100;
        if (since == null) {
            // 수집 기록이 없으면 최근 1년치로 제한
            since = OffsetDateTime.parse("2019-01-01T00:00:00Z");
        }
        String query = "author:" + username + " created:>=" + since.toInstant().toString();

        Set<String> result = new HashSet<>();
        while (true) {
            SearchIssuesResponse response = githubRestClient.request()
                    .endpoint("/search/issues")
                    .queryParam("q", query)
                    .queryParam("page", String.valueOf(page))
                    .queryParam("per_page", String.valueOf(perPage))
                    .get(SearchIssuesResponse.class);

            if (response.items() == null || response.items().isEmpty()) {
                break;
            }

            response.items().stream()
                    .map(SearchIssuesDto::repositoryUrl)
                    .map(url -> {
                        // repository_url 예: https://api.github.com/repos/{owner}/{repo}
                        String[] parts = url.split("/repos/");
                        return parts.length == 2 ? parts[1] : null;
                    })
                    .filter(name -> name != null)
                    .forEach(result::add);

            // 반환된 결과가 per_page보다 적으면 마지막 페이지
            if (response.items().size() < perPage) {
                break;
            }

            page++;

            // GitHub Search API는 최대 1000개의 결과만 반환
            if (page * perPage >= 1000) {
                break;
            }
        }
        return result;
    }

    /**
     * 3. username의 이벤트에서 기여한 repo의 full_name 추출
     */
    private Set<String> fetchReposFromEvents(String username) {
        List<EventRepoDto> events = new ArrayList<>();
        // event API는 최대 300개 이벤트까지만 제공
        for(int page = 1; page <= 3; page++){
            List<EventRepoDto> pageEvents = githubRestClient.request()
                    .endpoint("/users/" + username + "/events/public")
                    .queryParam("page", String.valueOf(page))
                    .queryParam("per_page", "100")
                    .getList(new ParameterizedTypeReference<>() {});
            if(pageEvents == null || pageEvents.isEmpty()){
                break;
            }
            events.addAll(pageEvents);
        }
        Set<String> result = new HashSet<>();
        events.stream()
                .map(EventRepoDto::repo)
                .map(EventRepoDto.RepoNameDto::name)
                .forEach(result::add);
        return result;
    }

}
