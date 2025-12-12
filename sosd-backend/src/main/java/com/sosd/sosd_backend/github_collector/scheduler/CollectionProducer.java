package com.sosd.sosd_backend.github_collector.scheduler;

import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryEntity;
import com.sosd.sosd_backend.repository.github.AccountRepoLinkRepository;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.service.github.GithubAccountRepositoryLinkService;
import com.sosd.sosd_backend.service.github.GithubAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionProducer {
    private final AccountRepoLinkRepository linkRepository;
    private final GithubAccountRepository accountRepository;

    private final GithubAccountRepositoryLinkService linkService;
    private final GithubAccountService accountService;

    private final JobScheduler jobScheduler;

    // ==================================================================
    // 1. Commit/PR/Issue Sync 스케줄러 (기여내역 수집)
    // 주기: 15초
    // ==================================================================
    @Scheduled(fixedRate = 15000)
    @Transactional
    public void produceContributionCollectionJobs() {

        // 1) 수집 대상 선택
        Pageable limit = PageRequest.of(0, 30);
        List<GithubAccountRepositoryEntity> targets =
                linkRepository.findReadyTargets(LocalDateTime.now(), limit);
        if (targets.isEmpty()) {
            log.info("[Scheduler] No contribution collection targets found.");
            return;
        }

        // 2) 수집 작업 스케줄링
        int jobCount = 0;
//        for (GithubAccountRepositoryEntity target : targets) {
//            // 상태를 QUEUED로 변경 (Locking)
//            linkService.markAsQueued(target.getId().getGithubAccountId(), target.getId().getGithubRepoId());
//
//            // JobRunr 작업 스케줄링
//        }

        log.info("[Scheduler] Enqueued {} repo sync jobs.", jobCount);

    }

    // ==================================================================
    // 2. User Scan 스케줄러 (신규 레포 탐색)
    // 주기: 1분
    // ==================================================================
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void produceUserScanJobs() {

    }

}
