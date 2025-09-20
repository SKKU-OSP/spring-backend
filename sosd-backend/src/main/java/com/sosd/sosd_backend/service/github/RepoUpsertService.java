package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.dto.github.GithubRepositoryUpsertDto;

import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RepoUpsertService {

    private final GithubRepositoryRepository githubRepositoryRepository;
    private final GithubAccountRepository githubAccountRepository;

    @Transactional
    public List<RepoRef> upsertRepos(List<GithubRepositoryUpsertDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }

        // 1) 입력 검증 + 키 수집
        Map<Long, GithubRepositoryUpsertDto> byId = new LinkedHashMap<>();
        for (var dto : dtos) {
            if (dto.githubRepoId() != null) byId.put(dto.githubRepoId(), dto);
        }

        if (byId.isEmpty()) return List.of();

        // 2) 기존 엔티티 벌크 로딩
        List<GithubRepositoryEntity> existing =
                githubRepositoryRepository.findAllByGithubRepoIdIn(new ArrayList<>(byId.keySet()));
        Map<Long, GithubRepositoryEntity> loaded = new HashMap<>(existing.size() * 2);
        for (var e : existing) loaded.put(e.getGithubRepoId(), e);

        // 3) 머지/신규 생성
        List<GithubRepositoryEntity> toSave = new ArrayList<>(byId.size());
        for (var dto : byId.values()) {
            var e = loaded.get(dto.githubRepoId());
            if (e == null) e = GithubRepositoryEntity.from(dto);
            else e.merge(dto);
            toSave.add(e);
        }

        // 4) 저장
        List<GithubRepositoryEntity> saved = githubRepositoryRepository.saveAll(toSave);

        // 5) RepoRef로 매핑하여 반환
        return saved.stream()
                .map(e -> new RepoRef(
                        e.getId(),
                        e.getGithubRepoId(),
                        e.getOwnerName(),
                        e.getRepoName(),
                        e.getOwnerName() + "/" + e.getRepoName()
                ))
                .toList();
    }


}
