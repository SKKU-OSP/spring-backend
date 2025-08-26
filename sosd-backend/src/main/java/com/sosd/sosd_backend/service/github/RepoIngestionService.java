package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.dto.github.GithubRepositoryUpsertDto;
import com.sosd.sosd_backend.github_collector.dto.response.GithubRepositoryResponseDto;
import com.sosd.sosd_backend.github_collector.collector.RepoCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepoIngestionService {

    private final RepoCollector repoCollector;
    private final RepoUpsertService repoUpsertService;


    /**
     * 주어진 GitHub 계정의 기여 저장소 목록을 수집하고, DB에 저장(upsert)까지 진행합니다.
     *
     * <ul>
     *   <li>외부 GitHub API를 호출하여 해당 계정의 기여 저장소 목록을 수집</li>
     *   <li>수집된 데이터를 {@link RepoUpsertService}를 통해 DB에 upsert 처리</li>
     *   <li>이 메서드 자체는 트랜잭션을 열지 않으며, DB 작업은 {@code RepoUpsertService} 내에서 처리됨</li>
     * </ul>
     *
     * @param githubLoginUsername GitHub 계정 로그인 ID
     */

    public void ingestGithubAccount(String githubLoginUsername){
        // 1) 깃허브 api를 통해 수집
        List<GithubRepositoryResponseDto> collectedDto = repoCollector.getAllContributedRepos(githubLoginUsername);

        // 2) GithubRepositoryUpsertDto로 변환
        List<GithubRepositoryUpsertDto> upsertDtos = collectedDto.stream()
                .map(GithubRepositoryUpsertDto::from)
                        .toList();

        // 3) 저장
        repoUpsertService.upsertRepos(upsertDtos);

    }

}
