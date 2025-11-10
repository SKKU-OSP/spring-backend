package com.sosd.sosd_backend.data_aggregation.service;


import com.sosd.sosd_backend.data_aggregation.entity.GithubContributionStats;
import com.sosd.sosd_backend.data_aggregation.repository.*;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionStatsService {
    private final GithubRepositoryRepository repositoryRepository;
    private final GithubCommitRepository commitRepository;
    private final GithubPullRequestRepository prRepository;
    private final GithubIssueRepository issueRepository;
    private final GithubContributionStatsRepository statsRepository;

    public List<GithubContributionStats> calculateUserStats(GithubAccount account, int year){
        List<GithubContributionStats> results = new ArrayList<>();
        Long githubId = account.getGithubId();
        List<GithubRepositoryEntity> repos = repositoryRepository.findReposNeedUpdate(githubId, account.getUserAccount(), year);


    }
}
