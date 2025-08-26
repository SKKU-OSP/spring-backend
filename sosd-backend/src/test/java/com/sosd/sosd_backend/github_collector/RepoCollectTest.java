package com.sosd.sosd_backend.github_collector;

import com.sosd.sosd_backend.github_collector.dto.response.GithubRepositoryResponseDto;
import com.sosd.sosd_backend.github_collector.collector.RepoCollector;
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
    void testGetRepoInfo(){
        GithubRepositoryResponseDto testRepoDto = repoCollector.getRepoInfo("SKKU-OSP", "SKKU-OSP");

        assertEquals(503242724L, testRepoDto.id(), "Repo ID가 일치하지 않습니다");
        assertEquals(107451259L, testRepoDto.owner().id(), "Owner ID가 일치하지 않습니다");

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
