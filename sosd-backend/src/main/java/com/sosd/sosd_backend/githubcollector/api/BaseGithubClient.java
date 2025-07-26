package com.sosd.sosd_backend.githubcollector.api;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public abstract class BaseGithubClient {

    private RestClient restClient;

    @Value("${github.api.token}")
    private String githubToken;

    private static final String BASE_URL = "https://api.github.com/";

    protected BaseGithubClient() {
        // 현재는 RestClient만 초기화해주면 돼서 비워줬지만 추후 필요하면 생성자 사용할 것
    }

    @PostConstruct
    private void initializeRestClient() {
        if(githubToken == null || githubToken.isEmpty()) {
            throw new IllegalStateException("Github 토큰이 설정되지 않았습니다.");
        }

        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "sosd-collector")
                .build();
    }

    protected <T> T get(
            String endpoint,
            Class<T> responseEntityType
    ){
        return get(endpoint, responseEntityType, Collections.emptyMap(), Collections.emptyMap());
    }

    protected <T> T get(
            String endpoint,
            Class<T> responseEntityType,
            Map<String, String> queryParams
    ){
        return get(endpoint, responseEntityType, queryParams, Collections.emptyMap());
    }


    protected <T> T get(
            String endpoint,
            Class<T> responseEntityType,
            Map<String, String> queryParams,
            Map<String, String> headers
    ){
        try{
            return restClient
                    .get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(endpoint);
                        queryParams.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    })
                    .headers(httpHeaders -> headers.forEach(httpHeaders::add))
                    .retrieve()
                    .body(responseEntityType);
        }
        catch (Exception ex){
            throw new RuntimeException("Github API 호출 실패: " + ex.getMessage(), ex);
        }
    }

    protected <T> T getPage(
            String endpoint,
            Class<T> responseEntityType,
            int page,
            int perPage,
            Map<String, String> queryParams,
            Map<String, String> headers
    ) {
        Map<String, String> paginatedParams = new HashMap<>(queryParams);
        paginatedParams.put("page", String.valueOf(page));
        paginatedParams.put("per_page", String.valueOf(perPage));
        return get(endpoint, responseEntityType, paginatedParams, headers);
    }

}