package com.sosd.sosd_backend.github_collector.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GithubGraphQLClient {

    private static final String GITHUB_GRAPHQL_URL = "https://api.github.com/graphql";
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
            String currentToken = tokenManager.getAvailableToken();

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

        public <T> T executeWithCursor(Class<T> responseType, String after) {
            if (after != null) {
                variable("after", after); // 기본 변수명 'after'로 고정
            }
            GraphQLResponse<T> response = execute(responseType);
            return response.getData();
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
