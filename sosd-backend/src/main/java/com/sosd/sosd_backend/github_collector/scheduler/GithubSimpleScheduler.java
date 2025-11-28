package com.sosd.sosd_backend.github_collector.scheduler;

import com.sosd.sosd_backend.entity.user.UserAccount;
import com.sosd.sosd_backend.github_collector.orchestrator.UserCollectionOrchestrator;
import com.sosd.sosd_backend.github_collector.dto.ref.UserAccountRef;
import com.sosd.sosd_backend.repository.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubSimpleScheduler {

    private final UserAccountRepository userAccountRepository;
    private final UserCollectionOrchestrator userCollectionOrchestrator;

    public void run(){

        log.info("Start Github Simple Scheduler");

        // 1. 활동 사용자 계정 모두 가져오기
        List<UserAccount> userAccounts = userAccountRepository.findAllByIsActiveTrue();

        // 전체 유저 수 확인
        int totalCount = userAccounts.size();
        int currentIdx = 0;

        // 2. 각 계정별로 수집 로직 실행
        for(UserAccount userAccount : userAccounts){
            currentIdx++; // 순번 증가

            log.info("Current Progress: {}/{} users", currentIdx, totalCount);

            MDC.put("userCtx", "User:" + userAccount.getStudentId());
            try{
                UserAccountRef userAccountRef = userAccount.toUserRef();
                userCollectionOrchestrator.collectByUser(userAccountRef);
            } finally {
                MDC.remove("userCtx");
            }
        }
        log.info("End Github Simple Scheduler");
    }
}
