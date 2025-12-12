package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubAccountService {

    private final GithubAccountRepository githubAccountRepository;

    @Transactional
    public void updateLastCrawling(Long githubId, LocalDateTime lastCrawling) {
        githubAccountRepository.updateLastCrawlingByGithubId(githubId, lastCrawling);
    }

    /**
     * 유저 스캔(Repo Discovery) 후 스케줄 업데이트
     */
    @Transactional
    public void updateScanSchedule(Long githubId, int newWeight, LocalDateTime nextScanDate) {
        GithubAccount account = githubAccountRepository.findByGithubId(githubId)
                .orElseThrow(() -> new EntityNotFoundException("Github Account not found: " + githubId));

        account.updateScanSchedule(newWeight, nextScanDate);

    }
}
