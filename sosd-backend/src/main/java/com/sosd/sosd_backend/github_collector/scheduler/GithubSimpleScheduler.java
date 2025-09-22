package com.sosd.sosd_backend.github_collector.scheduler;

import com.sosd.sosd_backend.entity.user.UserAccount;
import com.sosd.sosd_backend.github_collector.orchestrator.UserCollectionOrchestrator;
import com.sosd.sosd_backend.github_collector.dto.ref.UserAccountRef;
import com.sosd.sosd_backend.repository.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

        // 2. 각 계정별로 수집 로직 실행
        for(UserAccount userAccount : userAccounts){
            UserAccountRef userAccountRef = userAccount.toUserRef();
            userCollectionOrchestrator.collectByUser(userAccountRef);
        }
        log.info("End Github Simple Scheduler");
    }
}
