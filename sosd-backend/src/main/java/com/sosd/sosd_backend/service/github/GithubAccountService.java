package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
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
}
