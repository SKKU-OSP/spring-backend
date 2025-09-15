package com.sosd.sosd_backend.service.user;


import com.sosd.sosd_backend.dto.user.RecentRepoResponse;
import com.sosd.sosd_backend.dto.user.RepoIdWithDate;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.repository.github.GithubCommitRepository;
import com.sosd.sosd_backend.repository.github.GithubPullRequestRepository;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubService {
    private final GithubCommitRepository commitRepository;
    private final GithubRepositoryRepository repositoryRepository;
    private final GithubPullRequestRepository pullRequestRepository;

    // 가장 최근 레포지토리 4개 수집 api
    public List<RecentRepoResponse> getRecentRepos(Long githubId){
        List<RepoIdWithDate> recentRepoIds = commitRepository.findRecentRepoIds(githubId);
        List<RecentRepoResponse> recentRepoResponses = new ArrayList<>();

        for(RepoIdWithDate repoData : recentRepoIds){
            Long repoId = repoData.getRepoId();
            GithubRepositoryEntity repo = repositoryRepository.findById(repoId).orElseThrow(() -> new IllegalArgumentException("Repo not found: " + repoId));
            Long commitsCount = commitRepository.countByRepoId(repoId);
            Long prsCount = pullRequestRepository.countByRepoId(repoId);

            recentRepoResponses.add(new RecentRepoResponse(
                    repo.getRepoName(),
                    githubId,
                    repoData.getLastCommitDate(),
                    repo.getDescription(),
                    repo.getStar(),
                    commitsCount,
                    prsCount
            ));
        }
        return recentRepoResponses;
    }
}
