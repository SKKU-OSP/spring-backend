package com.sosd.sosd_backend.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;

@Configuration
public class GithubClientConfig {
    private static final String GITHUB_BASE_URL = "https://api.github.com";

    @Bean
    public RestClient githubClient() {
        // 1. Connection Pool 설정
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);

        // 2. HttpClient 생성
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        // 3. Factory 생성
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(3000);

        // 4. RestClient 반환 (기본 헤더 등 공통 설정 포함)
        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl(GITHUB_BASE_URL)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("User-Agent", "sosd-collector")
                .build();

    }
}
