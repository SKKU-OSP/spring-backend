package com.sosd.sosd_backend.github_collector.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GithubGraphQLClient {

    private static final String GITHUB_GRAPHQL_URL = "https://api.github.com/graphql";
    private static final int MAXIMUM_RETRIES = 3;
    private final GithubTokenManager tokenManager;
    private final RestClient baseClient;
    private final ObjectMapper objectMapper;

    public GithubGraphQLClient(GithubTokenManager tokenManager, ObjectMapper objectMapper) {
        this.tokenManager = tokenManager;
        this.objectMapper = objectMapper;
        this.baseClient = RestClient.builder()
                .baseUrl(GITHUB_GRAPHQL_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "sosd-collector")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public QueryBuilder query(String query) {
        return new QueryBuilder(baseClient, tokenManager, objectMapper, query);
    }

    public static class QueryBuilder {
        private final RestClient restClient;
        private final GithubTokenManager tokenManager;
        private final ObjectMapper objectMapper;
        private final String query;
        private final Map<String, Object> variables = new HashMap<>();
        private final Map<String, String> headers = new HashMap<>();

        private QueryBuilder(RestClient restClient, GithubTokenManager tokenManager,
                             ObjectMapper objectMapper, String query) {
            this.restClient = restClient;
            this.tokenManager = tokenManager;
            this.objectMapper = objectMapper;
            this.query = query;
        }

        public QueryBuilder variable(String key, Object value) {
            variables.put(key, value);
            return this;
        }

        public QueryBuilder variables(Map<String, Object> variables) {
            if (variables != null) {
                this.variables.putAll(variables);
            }
            return this;
        }

        public QueryBuilder header(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public <T> GraphQLResponse<T> execute(Class<T> responseType) {
            String currentToken = tokenManager.getToken();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            if (!variables.isEmpty()) {
                requestBody.put("variables", variables);
            }

            RestClient.RequestBodySpec spec = restClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + currentToken)
                    .body(requestBody);

            headers.forEach(spec::header);

            JsonNode jsonResponse = spec.retrieve().body(JsonNode.class);
            return parseGraphQLResponse(jsonResponse, responseType);
        }


        /** 레이트리밋 시 토큰을 자동 회전하며 재시도 (최대 회전 횟수 지정) */
        public <T> GraphQLResponse<T> executeWithAutoRotate(Class<T> responseType) {
            int rotations = 0;
            String token = tokenManager.getToken();

            while (true) {
                try {
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("query", query);
                    if (!variables.isEmpty()) {
                        requestBody.put("variables", variables);
                    }

                    RestClient.RequestBodySpec spec = restClient.post()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .body(requestBody);

                    headers.forEach(spec::header);

                    JsonNode json = spec.retrieve().body(JsonNode.class);

                    GraphQLResponse<T> parsed = parseGraphQLResponse(json, responseType);

                    // ----- (B) GraphQL 레벨 rate limit 처리 -----
                    if (isGraphqlRateLimited(parsed.getErrors())) {
                        if (rotations++ < MAXIMUM_RETRIES) {
                            log.warn("[rateLimit] GraphQL rate-limited. Rotating token ({}/{})", rotations, MAXIMUM_RETRIES);
                            token = tokenManager.getNextToken();
                            continue;
                        }
                        log.warn("[rateLimit] GraphQL rate-limited and max rotations reached ({}). Returning last response.", MAXIMUM_RETRIES);
                    }

                    return parsed;
                } catch (HttpClientErrorException e) {
                    int status = e.getStatusCode().value();

                    // HTTP 레벨 인증 문제 처리
                    if ((status == 401 || status == 403) && rotations < MAXIMUM_RETRIES) {
                        log.warn("[auth] {} from GitHub. Rotating token ({}/{})", status, rotations + 1, MAXIMUM_RETRIES);
                        token = tokenManager.getNextToken();
                        rotations++;
                        continue;
                    }

                    throw e;
                }
            }
        }

        private boolean isGraphqlRateLimited(List<GraphQLError> errors) {
            if (errors == null || errors.isEmpty()) return false;
            for (GraphQLError err : errors) {
                String msg = err.getMessage() == null ? "" : err.getMessage().toLowerCase();

                // extensions가 null일 수 있으므로 NPE 방어
                String code = null;
                String type = null;
                if (err.getExtensions() != null) {
                    Object codeObj = err.getExtensions().get("code");
                    Object typeObj = err.getExtensions().get("type");
                    code = codeObj == null ? null : codeObj.toString();
                    type = typeObj == null ? null : typeObj.toString();
                }

                if ("RATE_LIMIT".equalsIgnoreCase(err.getType())) return true;
                if ("RATE_LIMIT".equalsIgnoreCase(type)) return true;
                if ("RATE_LIMITED".equalsIgnoreCase(type)) return true;
                if ("RATE_LIMIT".equalsIgnoreCase(code)) return true;
                if ("RATE_LIMITED".equalsIgnoreCase(code)) return true;
                if (msg.contains("rate limit exceeded")) return true;
                if (msg.contains("api rate limit exceeded")) return true;
            }
            return false;
        }




        private <T> GraphQLResponse<T> parseGraphQLResponse(JsonNode jsonNode, Class<T> responseType) {
            GraphQLResponse<T> response = new GraphQLResponse<>();

            if (jsonNode.has("data") && !jsonNode.get("data").isNull()) {
                T data = objectMapper.convertValue(jsonNode.get("data"), responseType);
                response.setData(data);
            }

            if (jsonNode.has("errors")) {
                List<GraphQLError> errors = objectMapper.convertValue(
                        jsonNode.get("errors"),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, GraphQLError.class)
                );
                response.setErrors(errors);
            }

            return response;
        }
    }

    @Data
    public static class GraphQLResponse<T> {
        private T data;
        private List<GraphQLError> errors;
    }

    @Data
    public static class GraphQLError {
        private String type;
        private String message;
        private List<Location> locations;
        private List<String> path;
        private Map<String, Object> extensions;

        @Data
        public static class Location {
            private int line;
            private int column;
        }
    }

}
