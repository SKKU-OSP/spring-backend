package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.repository.github.AccountRepoLinkRepository;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
/**
 * 개발자의 테스트를 위한 서비스입니다
 */
public class RepoQueryService {

    private final GithubAccountRepository accountRepo;
    private final AccountRepoLinkRepository linkRepo;

    @Transactional(readOnly = true)
    public List<GithubRepositoryEntity> listReposByLoginUsername(String loginUsername) {
        var account = accountRepo.findByGithubLoginUsername(loginUsername)
                .orElseThrow(() -> new IllegalArgumentException("No account for username=" + loginUsername));

        // 조인 테이블 통해 레포 조회
        var repos = linkRepo.findReposByAccountId(account.getGithubId());
        // 보기에 편하게 최신 업데이트 순으로 정렬 (원하면 제거)
        repos.sort(Comparator.comparing(
                GithubRepositoryEntity::getGithubRepositoryUpdatedAt).reversed());
        return repos;
    }
}
