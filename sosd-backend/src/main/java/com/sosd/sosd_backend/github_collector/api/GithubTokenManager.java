package com.sosd.sosd_backend.github_collector.api;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class GithubTokenManager {

    private final List<String> tokens;
    private final int size;
    private final AtomicInteger idx = new AtomicInteger(0);

    public GithubTokenManager(@Value("${github.token}") List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("No GitHub tokens configured");
        }
        this.tokens = tokens.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
        this.size = this.tokens.size();
        log.info("GithubTokenManager initialized with {} tokens", size);
    }

    /** 현재 idx의 토큰을 반환 (advance 없음) */
    public String getToken() {
        int i = Math.floorMod(idx.get(), size);
        return tokens.get(i);
    }

    /** idx를 +1 하고 그 토큰을 반환 (라운드로빈 회전) */
    public String getNextToken() {
        int i = Math.floorMod(idx.incrementAndGet(), size);
        return tokens.get(i);
    }

}
