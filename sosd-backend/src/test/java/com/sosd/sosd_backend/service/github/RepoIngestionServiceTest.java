package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.entity.github.GithubRepository;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class RepoIngestionServiceTest {

    @Autowired
    private RepoIngestionService repoIngestionService;

    @Autowired
    private GithubRepositoryRepository githubRepositoryRepository;

    @Test
    void ingestUserRepo_save_test(){
        // given
        // 해당 유저가 테이블에 존재해야만 함
        // 원래 서비스 흐름이 유저 조회 -> 유저 단위로 레포 수집이기 때문에 실제론 유저가 없을 일은 없을듯..?
        String githubLoginUsername = "byungKHee";

        // when
        repoIngestionService.ingestGithubAccount(githubLoginUsername);

        // then
        Iterable<GithubRepository> all = githubRepositoryRepository.findAll();

        assertThat(all).isNotEmpty();
        all.forEach(repo -> {
            System.out.println(repo.getOwnerName() + "/" + repo.getRepoName());
        });
    }
}
