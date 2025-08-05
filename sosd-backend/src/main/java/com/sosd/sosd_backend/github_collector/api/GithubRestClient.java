package com.sosd.sosd_backend.github_collector.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GithubRestClient {

    private static final String GITHUB_BASE_URL = "https://api.github.com";
    private final RestClient restClient;

    public GithubRestClient(@Value("${github.token}") String githubToken) {
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