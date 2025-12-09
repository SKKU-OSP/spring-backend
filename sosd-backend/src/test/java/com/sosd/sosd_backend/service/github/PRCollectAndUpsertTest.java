package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.github_collector.collector.PullRequestCollector;
import com.sosd.sosd_backend.github_collector.dto.collect.context.PullRequestCollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.TimeCursor;
import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;
import com.sosd.sosd_backend.github_collector.dto.response.GithubPullRequestResponseDto;
import com.sosd.sosd_backend.github_collector.mapper.PullRequestMapper;
import com.sosd.sosd_backend.service.github.PullRequestUpsertService;
import com.sosd.sosd_backend.repository.github.GithubPullRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PRCollectAndUpsertTest {

    @Autowired private PullRequestCollector pullRequestCollector;
    @Autowired private PullRequestMapper pullRequestMapper;
    @Autowired private PullRequestUpsertService pullRequestUpsertService;
    @Autowired private GithubPullRequestRepository prRepo;

    @Test
    @Transactional
    void collectThenUpsert_shouldSaveToDatabase() {
        // ✅ 테스트용 RepoRef, AccountRef
        RepoRef testRepoRef = new RepoRef(
                123L,
                123L,
                "SKKU-OSP",
                "spring-backend",
                "SKKU-OSP/spring-backend",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        GithubAccountRef testAccountRef = new GithubAccountRef(
                80045655L,
                "MDQ6VXNlcjgwMDQ1NjU1",
                "byungKHee",
                null,
                null,
                null,
                null
        );

        // ✅ 수집 Context
        PullRequestCollectContext ctx = new PullRequestCollectContext(
                testAccountRef,
                testRepoRef,
                OffsetDateTime.parse("2025-08-01T00:00:00Z")
        );

        // 1) GitHub API (혹은 Mock)로부터 PR 수집
        CollectResult<GithubPullRequestResponseDto, TimeCursor> result =
                pullRequestCollector.collect(ctx);

        System.out.println("=== Collect Result ===");
        System.out.println("Fetched count: " + result.fetchedCount());
        System.out.println("Total count: " + result.totalCount());

        // 2) 매핑: ResponseDto → UpsertDto
        var dtos = result.results().stream()
                .map(r -> pullRequestMapper.toUpsertDto(r, testRepoRef.repoId(), testAccountRef.githubId()))
                .toList();

        // 3) 업서트 실행 (DB 저장)
        pullRequestUpsertService.upsertSingleTargetFromDtos(testRepoRef.repoId(), testAccountRef.githubId(), dtos);

        // 4) 검증: 실제 DB에 저장된 PR 확인
        var saved = prRepo.findAll();
        System.out.println("=== Saved in DB ===");
        saved.forEach(pr ->
                System.out.printf("PR #%d [%s] %s (id=%d)%n",
                        pr.getPrNumber(),
                        pr.getIsOpen() ? "OPEN" : "CLOSED",
                        pr.getPrTitle(),
                        pr.getGithubPrId())
        );

        assertThat(saved).isNotEmpty();
    }
}
