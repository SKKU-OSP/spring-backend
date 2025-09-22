package com.sosd.sosd_backend.dto.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigInteger;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public interface RepoGuidelineResponse {
    String getOwnerId();
    String getRepoName();
    LocalDateTime getCreateDate();
    LocalDateTime getUpdateDate();
    Integer getContributorsCount();
    String getReleaseVer();
    Integer getReleaseCount();
    Integer getReadme();
    String getLicense();
    String getProjShortDesc();
    Integer getStarCount();
    Integer getWatcherCount();
    Integer getForkCount();
    Integer getDependencyCount();
    Long getCommitCount();
    Long getPrCount();
    Long getOpenIssueCount();
    Long getCloseIssueCount();
    LocalDateTime getCommitterDate();
}
