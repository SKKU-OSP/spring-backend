package com.sosd.sosd_backend.github_collector.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GithubSimpleSchedulerTest {

    @Autowired
    private GithubSimpleScheduler githubSimpleScheduler;

    @Test
    void testRunScheduler() {
        githubSimpleScheduler.run();
    }
}
