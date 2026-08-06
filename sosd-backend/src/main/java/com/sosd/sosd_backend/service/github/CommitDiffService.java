package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.dto.api.CommitDiffApiDto;
import com.sosd.sosd_backend.github_collector.api.GithubRestClient;
import com.sosd.sosd_backend.github_collector.dto.response.GithubCommitDetailResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommitDiffService {

    private final GithubRestClient githubRestClient;

    public CommitDiffApiDto getDiff(String owner, String repo, String sha) {
        String endpoint = "/repos/" + owner + "/" + repo + "/commits/" + sha;
        log.info("커밋 diff 조회: {}", endpoint);

        GithubCommitDetailResponseDto detail = githubRestClient.request()
                .endpoint(endpoint)
                .get(GithubCommitDetailResponseDto.class);

        List<CommitDiffApiDto.FileDiff> files = detail.files() == null
                ? List.of()
                : detail.files().stream()
                        .map(f -> CommitDiffApiDto.FileDiff.builder()
                                .filename(f.filename())
                                .status(f.status())
                                .additions(f.additions() != null ? f.additions() : 0)
                                .deletions(f.deletions() != null ? f.deletions() : 0)
                                .patch(f.patch())
                                .build())
                        .toList();

        return CommitDiffApiDto.builder()
                .sha(sha)
                .owner(owner)
                .repo(repo)
                .files(files)
                .build();
    }
}