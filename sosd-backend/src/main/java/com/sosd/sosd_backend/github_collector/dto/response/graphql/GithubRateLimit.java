package com.sosd.sosd_backend.github_collector.dto.response.graphql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubRateLimit(
        Integer cost,
        Integer remaining,
        Integer used,
        Integer limit,
        OffsetDateTime resetAt
) {}
