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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.sql.Time;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
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
        // TODO: 모든 레포를 다 가져오는건 코스트가 상당히 크기 때문에 적절한 정책 생각해볼 것
//        // 1-1. 사용자의 모든 공개 repo 목록에서 추출 - 수집기록이 없으면 실행
//        if (context.lastCrawling() == null || context.lastCrawling().isBefore(OffsetDateTime.now().minusDays(7))) {
//            fullNames.addAll(fetchReposFromUserRepos(context.githubAccountRef().githubLoginUsername()));
//        }
//        // 1-2. 사용자가 최근에 기여한 이슈/PR에서 repo 추출 - 수집기록이 없거나 2일 이상 지난 경우 실행
//        if (context.lastCrawling() == null || context.lastCrawling().isBefore(OffsetDateTime.now().minusDays(2))) {
//            fullNames.addAll(fetchReposFromSearchIssues(context.githubAccountRef().githubLoginUsername(), context.lastCrawling()));
//        }
//        // 1-3. 사용자의 이벤트에서 기여한 repo 추출 - 항상 실행
//        fullNames.addAll(fetchReposFromEvents(context.githubAccountRef().githubLoginUsername()));

        // 1-1. 사용자의 모든 공개 repo 목록에서 추출
        fullNames.addAll(fetchReposFromUserRepos(context.githubAccountRef().githubLoginUsername()));
//        // 1-2. 사용자가 최근에 기여한 이슈/PR에서 repo 추출
//        fullNames.addAll(fetchReposFromSearchIssues(context.githubAccountRef().githubLoginUsername(), context.lastCrawling()));
        // 1-3. 사용자의 이벤트에서 기여한 repo 추출
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
                    nameWithOwner
                    isPrivate
                    defaultBranchRef {\s
                        name
                        target {
                        ... on Commit {
                          history {
                            totalCount
                          }
                        }
                      }   \s
                    }
                    description
                    stargazerCount
                    watchers { totalCount }
                    forkCount
                    licenseInfo { name }
                    createdAt
                    updatedAt
                    pushedAt
                
                    object(expression: "HEAD:README.md") {
                      ... on Blob { text }
                    }
                
                    languages(first: 5, orderBy: {field: SIZE, direction: DESC}) {
                      totalSize
                      edges {
                        size
                        node { name }
                      }
                    }
                
                    dependencyGraphManifests(first: 1) {
                      totalCount
                    }
                    mentionableUsers(first: 0) { totalCount }
                
                
                    # Pull Request 수 (alias 사용)
                    openPRs: pullRequests(states: OPEN) {
                      totalCount
                    }
                    closedPRs: pullRequests(states: CLOSED) {
                      totalCount
                    }
                    mergedPRs: pullRequests(states: MERGED) {
                      totalCount
                    }
                
                    # Issue 수 (alias 사용)
                    openIssues: issues(states: OPEN) {
                      totalCount
                    }
                    closedIssues: issues(states: CLOSED) {
                      totalCount
                    }
                  }
                
                  rateLimit {
                    cost
                    used
                    remaining
                    limit
                    resetAt
                  }
                }
                """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("owner", owner);
        variables.put("name", name);

        var res = githubGraphQLClient.query(query)
                .variables(variables)
                .executeWithAutoRotate(GithubRepositoryGraphQLResult.class);

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
        Set<String> result = new HashSet<>();
        int page = 1;
        int perPage = 10;

        long startedNs = System.nanoTime();

        try {
            while (true) {
                List<UserRepoDto> repos = githubRestClient.request()
                        .endpoint("/users/" + username + "/repos")
                        .queryParam("type", "all")                 // forks 포함
                        .queryParam("per_page", String.valueOf(perPage))
                        .queryParam("page", String.valueOf(page))
                        .getList(new ParameterizedTypeReference<>() {});

                if (repos == null || repos.isEmpty()) {
                    break;
                }

                repos.stream()
                        .map(UserRepoDto::fullName)
                        .filter(Objects::nonNull)
                        .forEach(result::add);

                // 현재 페이지의 항목 수가 perPage보다 작으면 마지막 페이지
                if (repos.size() < perPage) {
                    break;
                }
                page++;
            }
        }
        catch (HttpClientErrorException e) {
            int sc = e.getStatusCode().value();

            // 계정 단위 스킵
            if (sc == 400 || sc == 404 || sc == 410 || sc == 422 || sc == 451) {
                log.warn("[userRepos] {} for user={}, skipping. body={}",
                        sc, username, e.getResponseBodyAsString());
                return result; // 빈 결과 리턴
            }
            // 토큰/환경 문제 → 위로
            if (sc == 401 || sc == 403) {
                log.error("[userRepos] {} for user={}, escalating. body={}",
                        sc, username, e.getResponseBodyAsString());
            }
            throw e; // 나머지는 그대로 위로
        }
        catch (ResourceAccessException | HttpServerErrorException e) {
            // 네트워크/서버 에러는 위로 → 상위에서 재시도/알람
            throw e;
        }

        long elapsedTimeMs = Math.round((System.nanoTime() - startedNs) / 1_000_000.0);
        log.info("  >> repo from api fetched={} elapsed={}ms", result.size(), elapsedTimeMs);

        return result;
    }

    /**
     * 2. username이 최근에 기여한 이슈/PR에서 repo의 full_name 추출
     */
    private Set<String> fetchReposFromSearchIssues(String username, OffsetDateTime since) {
        record SearchIssuesResponse(List<SearchIssuesDto> items) {}

        long startedNs = System.nanoTime();

        // 쿼리 파라미터 설정
        int page = 1;
        int perPage = 100;
        if (since == null) {
            // 수집 기록이 없으면 최근 1년치로 제한
            since = OffsetDateTime.parse("2019-01-01T00:00:00Z");
        }
        String query = "author:" + username + " created:>=" + since.toInstant().toString();

        Set<String> result = new HashSet<>();

        try{
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
        }
        catch (HttpClientErrorException e) {
            int sc = e.getStatusCode().value();

            if (sc == 400 || sc == 404 || sc == 410 || sc == 422 || sc == 451) {
                log.warn("[searchIssues] {} for user={}, skipping. body={}",
                        sc, username, e.getResponseBodyAsString());
                return result;
            }
            if (sc == 401 || sc == 403) {
                log.error("[searchIssues] {} for user={}, escalating. body={}",
                        sc, username, e.getResponseBodyAsString());
            }
            throw e;
        }
        catch (ResourceAccessException | HttpServerErrorException e) {
            throw e;
        }
        long elapsedTimeMs = Math.round((System.nanoTime() - startedNs) / 1_000_000.0);
        log.info("  >> repo from pr/issue history fetched={} elapsed={}ms", result.size(), elapsedTimeMs);
        return result;
    }

    /**
     * 3. username의 이벤트에서 기여한 repo의 full_name 추출
     */
    private Set<String> fetchReposFromEvents(String username) {
        List<EventRepoDto> events = new ArrayList<>();
        // event API는 최대 300개 이벤트까지만 제공

        long startedNs = System.nanoTime();

        try {
            for (int page = 1; page <= 3; page++) {
                List<EventRepoDto> pageEvents = githubRestClient.request()
                        .endpoint("/users/" + username + "/events/public")
                        .queryParam("page", String.valueOf(page))
                        .queryParam("per_page", "100")
                        .getList(new ParameterizedTypeReference<>() {
                        });
                if (pageEvents == null || pageEvents.isEmpty()) {
                    break;
                }
                events.addAll(pageEvents);
            }
        }
        catch (HttpClientErrorException e) {
            int sc = e.getStatusCode().value();

            if (sc == 400 || sc == 404 || sc == 410 || sc == 422 || sc == 451) {
                log.warn("[events] {} for user={}, skipping. body={}",
                        sc, username, e.getResponseBodyAsString());
                return Set.of(); // 빈 결과
            }
            if (sc == 401 || sc == 403) {
                log.error("[events] {} for user={}, escalating. body={}",
                        sc, username, e.getResponseBodyAsString());
            }
            throw e;
        }
        catch (ResourceAccessException | HttpServerErrorException e) {
            throw e;
        }

        long elapsedTimeMs = Math.round((System.nanoTime() - startedNs) / 1_000_000.0);
        log.info("  >> repo from events fetched={} elapsed={}ms", events.size(), elapsedTimeMs);

        Set<String> result = new HashSet<>();
        events.stream()
                .map(EventRepoDto::repo)
                .map(EventRepoDto.RepoNameDto::name)
                .forEach(result::add);
        return result;
    }

}
