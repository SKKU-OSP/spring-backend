package com.sosd.sosd_backend.github_collector.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GithubRestClient {

    private static final String GITHUB_BASE_URL = "https://api.github.com";
    private static final int MAXIMUM_RETRIES = 3;
    private static final Pattern RATE_LIMIT_PATTERN =
            Pattern.compile("api\\s+rate\\s+limit\\s+exceeded", Pattern.CASE_INSENSITIVE);

    private final GithubTokenManager tokenManager;
    private final RestClient baseClient;

    public GithubRestClient(GithubTokenManager tokenManager) {
        this.tokenManager = tokenManager;
        this.baseClient = RestClient.builder()
                .baseUrl(GITHUB_BASE_URL)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "sosd-collector")
                .build();
        log.debug("GithubRestClient initialized with TokenManager");
    }

    public RequestBuilder request(){
        return new RequestBuilder(baseClient, tokenManager);
    }

    public static class RequestBuilder{
        private final RestClient restClient;
        private final GithubTokenManager tokenManager;
        private String endpoint;
        private final Map<String, String> queryParams = new HashMap<>();
        private final Map<String, String> headers = new HashMap<>();

        private RequestBuilder(RestClient restClient, GithubTokenManager tokenManager){
            this.restClient = restClient;
            this.tokenManager = tokenManager;
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
            int rotations = 0;
            String token = tokenManager.getToken();

            while (true) {
                try {
                    ResponseEntity<T> entity = buildGetRequest(token)
                            .retrieve()
                            .toEntity(responseType);

                    if (isPrimaryRateLimited(entity.getHeaders())) {
                        if (rotations++ < MAXIMUM_RETRIES) {
                            log.warn("[rateLimit] REST rate-limited (header). Rotating token {}/{}", rotations, MAXIMUM_RETRIES);
                            token = tokenManager.getNextToken();
                            continue;
                        }
                    }
                    return entity.getBody();

                } catch (HttpClientErrorException.Forbidden e) {
                    if (bodyIndicatesRateLimit(e) && rotations++ < MAXIMUM_RETRIES) {
                        log.warn("[rateLimit] REST rate-limited (403 body). Rotating token {}/{}", rotations, MAXIMUM_RETRIES);
                        token = tokenManager.getNextToken();
                        continue;
                    }
                    throw e;
                }
            }
        }

        // GET 요청 - List
        public <T> List<T> getList(ParameterizedTypeReference<List<T>> responseType) {
            int rotations = 0;
            String token = tokenManager.getToken();

            while (true) {
                try {
                    ResponseEntity<List<T>> entity = buildGetRequest(token)
                            .retrieve()
                            .toEntity(responseType);

                    if (isPrimaryRateLimited(entity.getHeaders())) {
                        if (rotations++ < MAXIMUM_RETRIES) {
                            log.warn("[rateLimit] REST rate-limited (header, list). Rotating token {}/{}", rotations, MAXIMUM_RETRIES);
                            token = tokenManager.getNextToken();
                            continue;
                        }
                    }
                    List<T> body = entity.getBody();
                    return (body != null) ? body : List.of();

                } catch (HttpClientErrorException.Forbidden e) {
                    if (bodyIndicatesRateLimit(e) && rotations++ < MAXIMUM_RETRIES) {
                        log.warn("[rateLimit] REST rate-limited (403 body, list). Rotating token {}/{}", rotations, MAXIMUM_RETRIES);
                        token = tokenManager.getNextToken();
                        continue;
                    }
                    throw e;
                }
            }
        }

        // GET 요청 - 페이지네이션
        // TODO: 모든 페이지의 데이터를 들고 있다가 반환하면 메모리 낭비가 심해질 것 같아서 실시간 처리에 대해 고민
        // 커밋같은 경우는 페이지가 200개씩 넘게 나오는 분들도 계서서 고민해봐야 할 듯
        public <T> List<T> getAllPages(ParameterizedTypeReference<List<T>> responseType, int perPage) {
            List<T> results = new ArrayList<>();
            int page = 1;

            while(true){
                try {
                    queryParams.put("page", String.valueOf(page));
                    queryParams.put("per_page", String.valueOf(perPage));

                    List<T> pageResults = getList(responseType);

                    if (pageResults == null || pageResults.isEmpty()) {
                        break;
                    }

                    results.addAll(pageResults);

                    if (pageResults.size() < perPage) {
                        break;
                    }
                    page++;
                } catch (Exception e) {
                    System.err.println("페이지네이션 중단: page=" + page + ", error: " + e.getMessage());
                    break;
                }
            }
            return results;
        }

        private RestClient.RequestHeadersSpec<?> buildGetRequest(String token){
            // 요청 시점에 토큰을 동적으로 가져와서 설정

            RestClient.RequestHeadersSpec<?> spec = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(endpoint);
                        queryParams.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            // 추가 헤더 설정
            headers.forEach(spec::header);
            return spec;
        }

        private boolean isPrimaryRateLimited(HttpHeaders headers) {
            String remaining = headers != null ? headers.getFirst("X-RateLimit-Remaining") : null;
            return remaining != null && "0".equals(remaining);
        }

        private boolean bodyIndicatesRateLimit(HttpClientErrorException.Forbidden e) {
            String body = e.getResponseBodyAsString(StandardCharsets.UTF_8);
            return body != null && RATE_LIMIT_PATTERN.matcher(body).find();
        }
    }
}