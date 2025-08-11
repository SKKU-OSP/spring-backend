package com.sosd.sosd_backend.github_collector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RepoCollectorDtos {
    public record UserRepoDto(
            @JsonProperty("full_name") String fullName
    ){}

    public record SearchIssuesDto(
            @JsonProperty("repository_url") String repositoryUrl
    ) {}

    public record EventRepoDto(
            @JsonProperty("repo") RepoNameDto repo
    ) {
        public record RepoNameDto(@JsonProperty("name") String name) {}
    }

}
