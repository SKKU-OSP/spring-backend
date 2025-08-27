package com.sosd.sosd_backend.github_collector;

import com.sosd.sosd_backend.github_collector.dto.response.GithubRepositoryResponseDto;
import com.sosd.sosd_backend.github_collector.collector.RepoCollector;
import com.sosd.sosd_backend.github_collector.dto.response.graphql.GithubRepositoryGraphQLResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RepoCollectTest {

    @Autowired
    private RepoCollector repoCollector;

    @Test
    void testGetRepoInfoByGraphQL(){
        GithubRepositoryGraphQLResult testRepoDto = repoCollector.getRepoInfoGraphQL("SKKU-OSP", "SKKU-OSP");
        assertEquals(503242724, testRepoDto.repository().databaseId(), "Repo ID가 일치하지 않습니다");

        // 디버깅용 출력
        System.out.println(testRepoDto);
    }

    @Test
    void testGetAllReposFromUser(){
        List<GithubRepositoryResponseDto> repoLists = repoCollector.getAllContributedRepos("ki011127");
        for(GithubRepositoryResponseDto repo : repoLists){
            System.out.println(repo.fullName());
        }
    }
}
