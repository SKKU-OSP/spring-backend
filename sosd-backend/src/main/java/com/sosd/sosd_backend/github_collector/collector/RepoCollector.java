package com.sosd.sosd_backend.github_collector.collector;

import com.sosd.sosd_backend.entity.github.TestRepoDto;
import com.sosd.sosd_backend.github_collector.api.GithubRestClient;
import org.springframework.stereotype.Component;

@Component
public class RepoCollector {

    private final GithubRestClient githubRestClient;

    public RepoCollector(GithubRestClient githubRestClient) {
        this.githubRestClient = githubRestClient;
    }

    public TestRepoDto getRepoInfo(String owner, String name){
        return githubRestClient.request()
                .endpoint("/repos/" + owner + "/" + name)
                .get(TestRepoDto.class);
    }
}
