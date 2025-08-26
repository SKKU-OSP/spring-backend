package com.sosd.sosd_backend.github_collector.orchestrator;

import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.UserAccountRef;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserCollectionOrchestrator {

    List<GithubAccount> githubAccounts;
    GithubAccountRepository githubAccountRepository;
    GithubAccountCollectionOrchestrator githubAccountCollectionOrchestrator;

    /**
     * 단일 유저에 대한 수집 수행
     * 해당 유저가 가진 모든 깃허브 계정에 대한 수집 수행
     * @param userAccountRef
     */
    public void collectByUser(UserAccountRef userAccountRef){
        // 1. 각 유저별 깃허브 계정 조회
        githubAccounts = githubAccountRepository.findAllByUserAccount_StudentId(userAccountRef.studentId());

        // 2. 각 github 계정별 수집 실행
        for (GithubAccount githubAccount : githubAccounts){
            GithubAccountRef githubAccountRef = githubAccount.toGithubAccountRef();
            githubAccountCollectionOrchestrator.collectByGithubAccount(githubAccountRef);
        }

    }
}
