package com.sosd.sosd_backend.github_collector.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GithubRestClient {

    private static final String GITHUB_BASE_URL = "https://api.github.com";
    private final RestClient restClient;

    public GithubRestClient(@Value("${github.token}") String githubToken) {
        log.debug("GithubRestClient: githubToken={}", githubToken);
        this.restClient = RestClient.builder()
                .baseUrl(GITHUB_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "sosd-collector")
                .build();
    }

    public RequestBuilder request(){
        return new RequestBuilder(restClient);
    }

    public static class RequestBuilder{
        private final RestClient restClient;
        private String endpoint;
        private final Map<String, String> queryParams = new HashMap<>();
        private final Map<String, String> headers = new HashMap<>();

        private RequestBuilder(RestClient restClient){
            this.restClient = restClient;
        }

        public RequestBuilder endpoint(String endpoint){
            this.endpoint = endpoint;
            return this;
        }

        public RequestBuilder queryParam(String key, String value){
            queryParams.put(key, value);
            return this;
        }

        public RequestBuilder queryParams(Map<String, String> queryParams){
            if(queryParams != null){
                this.queryParams.putAll(queryParams);
            }
            return this;
        }

        public RequestBuilder header(String key, String value){
            headers.put(key, value);
            return this;
        }

        public RequestBuilder headers(Map<String, String> headers){
            if(headers != null){
                this.headers.putAll(headers);
            }
            return this;
        }

        // TODO: 에러 핸들링

        // GET 요청 - 단일 객체
        public <T> T get(Class<T> responseType) {
            RestClient.RequestHeadersSpec<?> request = buildGetRequest();
            return request.retrieve().body(responseType);
        }

        // GET 요청 - List
        public <T> List<T> getList(ParameterizedTypeReference<List<T>> responseType) {
            RestClient.RequestHeadersSpec<?> request = buildGetRequest();
            return request.retrieve().body(responseType);
        }

        // GET 요청 - 페이지네이션
        // TODO: 모든 페이지의 데이터를 들고 있다가 반환하면 메모리 낭비가 심해질 것 같아서 실시간 처리에 대해 고민
        // 커밋같은 경우는 페이지가 200개씩 넘게 나오는 분들도 계서서 고민해봐야 할 듯
        public <T> List<T> getAllPages(ParameterizedTypeReference<List<T>> responseType, int perPage) {
            List<T> results = new ArrayList<>();
            int page = 1;

            while(true){
                queryParams.put("page", String.valueOf(page));
                queryParams.put("per_page", String.valueOf(perPage));

                List<T> pageResults = getList(responseType);

                if(pageResults == null || pageResults.isEmpty()){
                    break;
                }

                results.addAll(pageResults);

                if(pageResults.size() < perPage){
                    break;
                }
                page++;
            }
            return results;
        }

        private RestClient.RequestHeadersSpec<?> buildGetRequest(){
            RestClient.RequestHeadersSpec<?> spec = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(endpoint);
                        queryParams.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    });

            headers.forEach(spec::header);
            return spec;
        }
    }

}