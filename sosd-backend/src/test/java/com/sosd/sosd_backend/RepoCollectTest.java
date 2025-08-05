package com.sosd.sosd_backend;

import com.sosd.sosd_backend.entity.github.TestRepoDto;
import com.sosd.sosd_backend.github_collector.collector.RepoCollector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RepoCollectTest {

    @Autowired
    private RepoCollector repoCollector;

    @Test
    void testGetRepoInfo(){
        TestRepoDto testRepoDto = repoCollector.getRepoInfo("SKKU-OSP", "SKKU-OSP");
        System.out.println(testRepoDto);
    }
}
