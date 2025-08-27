package com.sosd.sosd_backend.github_collector.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
public class GithubTokenManager {

    private final String githubToken;

    public GithubTokenManager(@Value("${github.token}") String githubToken) {
        this.githubToken = githubToken;
        log.debug("GithubTokenManager initialized with token");
    }

    /**
     * 사용 가능한 토큰 반환
     * 추후 여러 토큰 중 rate limit이 남은 토큰을 선택하는 로직으로 확장
     * @return github API token
     */
    public String getAvailableToken() {
        return githubToken;
    }

}
