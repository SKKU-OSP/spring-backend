package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.github_collector.dto.RepoRef;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class UserRepoIngestionServiceTest {

    @Autowired
    private UserRepoIngestionService userRepoIngestionService;

    @Autowired
    private GithubRepositoryRepository githubRepositoryRepository;

    @Test
    void ingestUserRepo_save_test(){
        // given
        // 해당 유저가 테이블에 존재해야만 함
        // 원래 서비스 흐름이 유저 조회 -> 유저 단위로 레포 수집이기 때문에 실제론 유저가 없을 일은 없을듯..?
        String githubLoginUsername = "byungKHee";

        // when
        List<RepoRef> refs = userRepoIngestionService.collectAndSaveUserRepos(githubLoginUsername);

        // then

        // 1) 반환값 null 체크
        assertThat(refs).isNotEmpty();
        assertThat(refs).allSatisfy(ref -> {
            assertThat(ref.repoId()).as("repoId").isNotNull();
            assertThat(ref.githubRepoId()).as("githubRepoId").isNotNull();
            assertThat(ref.ownerName()).as("ownerName").isNotBlank();
            assertThat(ref.repoName()).as("repoName").isNotBlank();
            assertThat(ref.fullName()).as("fullName").isEqualTo(ref.ownerName() + "/" + ref.repoName());
        });

        // 2) DB일치 검증
        List<Long> ghIds = refs.stream().map(RepoRef::githubRepoId).toList();
        List<GithubRepositoryEntity> saved = githubRepositoryRepository.findAllByGithubRepoIdIn(ghIds);
        assertThat(saved).isNotEmpty();

        assertThat(saved).hasSameSizeAs(refs);

        Map<Long, GithubRepositoryEntity> byGhId = saved.stream()
                .collect(Collectors.toMap(GithubRepositoryEntity::getGithubRepoId, e -> e));

        refs.forEach(ref -> {
            GithubRepositoryEntity e = byGhId.get(ref.githubRepoId());
            assertThat(e).as("entity for githubRepoId=" + ref.githubRepoId()).isNotNull();
            assertThat(e.getId()).as("internal PK matches").isEqualTo(ref.repoId());
            assertThat(e.getOwnerName()).isEqualTo(ref.ownerName());
            assertThat(e.getRepoName()).isEqualTo(ref.repoName());
        });


//        // 3) 수집 확인용 system print
//        saved.forEach(repo -> {
//            System.out.println(repo.getOwnerName() + "/" + repo.getRepoName());
//        });

    }
}
