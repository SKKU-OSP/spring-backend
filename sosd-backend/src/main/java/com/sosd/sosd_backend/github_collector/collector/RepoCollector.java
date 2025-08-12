package com.sosd.sosd_backend.github_collector.collector;

import com.sosd.sosd_backend.github_collector.dto.GithubRepositoryResponseDto;
import com.sosd.sosd_backend.github_collector.dto.RepoCollectorDtos.EventRepoDto;
import com.sosd.sosd_backend.github_collector.dto.RepoCollectorDtos.SearchIssuesDto;
import com.sosd.sosd_backend.github_collector.dto.RepoCollectorDtos.UserRepoDto;
import com.sosd.sosd_backend.github_collector.api.GithubRestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RepoCollector {

    private final GithubRestClient githubRestClient;

    public RepoCollector(GithubRestClient githubRestClient) {
        this.githubRestClient = githubRestClient;
    }

    /**
     * username이 기여한 모든 repo의 상세 정보를 반환
     */
    public List<GithubRepositoryResponseDto> getAllContributedRepos(String username) {
        Set<String> fullNames = new HashSet<>();

        fullNames.addAll(fetchReposFromUserRepos(username));
        fullNames.addAll(fetchReposFromSearchIssues(username));
        fullNames.addAll(fetchReposFromEvents(username));

//        for(String fullName : fullNames){
//            System.out.println(fullName);
//        }

        return fullNames.stream()
                .map(fullName -> {
                    String[] parts = fullName.split("/");
                    if (parts.length == 2) {
                        return getRepoInfo(parts[0], parts[1]);
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .toList();
    }

    /**
     * 단일 repo 정보 조회
     */
    public GithubRepositoryResponseDto getRepoInfo(String owner, String name){
        try {
            return githubRestClient.request()
                    .endpoint("/repos/" + owner + "/" + name)
                    .get(GithubRepositoryResponseDto.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            // 404 에러 발생 시 null 반환
            return null;
        }
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
    private Set<String> fetchReposFromSearchIssues(String username) {
        record SearchIssuesResponse(List<SearchIssuesDto> items) {}

        Set<String> result = new HashSet<>();
        int page = 1;
        int perPage = 100; // GitHub Search API는 최대 100개까지 지원

        // TODO: response 형태가 특이해서 일단 여기서 수동처리 했는데 client 객체에서 처리해야 할 지 고민
        // {
        //    "total_count": 16,
        //    "incomplete_results": false,
        //    "items": [
        //          {},
        //          {}, ....
        //    ]
        //}
        while (true) {
            SearchIssuesResponse response = githubRestClient.request()
                    .endpoint("/search/issues")
                    .queryParam("q", "author:" + username)
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
        List<EventRepoDto> events = githubRestClient.request()
                .endpoint("/users/" + username + "/events/public")
                .getAllPages(new ParameterizedTypeReference<>() {}, 100);

        Set<String> result = new HashSet<>();
        events.stream()
                .map(EventRepoDto::repo)
                .map(EventRepoDto.RepoNameDto::name)
                .forEach(result::add);
        return result;
    }

}
