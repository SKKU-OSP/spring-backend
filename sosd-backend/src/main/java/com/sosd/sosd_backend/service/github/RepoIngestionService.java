package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.dto.RepositoryDetailDto;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.github.GithubRepository;
import com.sosd.sosd_backend.github_collector.collector.RepoCollector;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepoIngestionService {

    private final RepoCollector repoCollector;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final GithubAccountRepository githubAccountRepository;


    /**
     * Github Account 단위 요청
     * github username을 받아서 기여한 모든 레포 수집 후 DB upsert
     */
    @Transactional
    public void ingestAccount(String githubLoginUsername){
        // 1) 깃허브 api를 통해 수집
        List<RepositoryDetailDto> dtos = repoCollector.getAllContributedRepos(githubLoginUsername);

        // 2) 저장
        upsertRepos(githubLoginUsername, dtos);

    }

    private void upsertRepos(String githubLoginUsername, List<RepositoryDetailDto> dtos){
        // 2-1) 계정 로딩
        GithubAccount account = githubAccountRepository.findByGithubLoginUsername(githubLoginUsername)
                .orElseThrow(()->new RuntimeException("GithubAccount 없음: " + githubLoginUsername));

        // 2-2)
        List<GithubRepository> entities = dtos.stream()
                .map(dto -> toEntity(dto, account))
                .toList();

        // 2-3) 저장 (repository는 보통 그렇게 많지 않기 때문에 네이티브 사용x)
        githubRepositoryRepository.saveAll(entities);
    }


    private GithubRepository toEntity(RepositoryDetailDto dto, GithubAccount account){
        return GithubRepository.builder()
                .repoId(dto.id())
                .ownerName(dto.ownerNameOnly())
                .repoName(dto.repoNameOnly())
                .defaultBranch(nvl(dto.defaultBranch(), "main"))
                .githubRepositoryCreatedAt(toUtcLocal(dto.createdAt()))
                .githubRepositoryUpdatedAt(toUtcLocal(dto.updatedAt()))
                .pushedAt(toUtcLocal(dto.pushedAt()))
                .githubAccount(account)
                .build();
    }

    private static LocalDateTime toUtcLocal(OffsetDateTime odt) {
        return odt != null ? odt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    private static String nvl(String v, String def) {
        return (v == null || v.isEmpty()) ? def : v;
    }

}
