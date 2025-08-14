package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.dto.github.GithubRepositoryUpsertDto;

import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.github_collector.dto.RepoRef;
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
        List<Long> ids = dtos.stream()
                .map(GithubRepositoryUpsertDto::githubRepoId)
                .filter(Objects::nonNull)
                .toList();
        if (ids.isEmpty()) return List.of();

        // 2) 기존 엔티티 벌크 로딩
        List<GithubRepositoryEntity> existing = githubRepositoryRepository.findAllByGithubRepoIdIn(ids);
        Map<Long, GithubRepositoryEntity> byRepoId = new HashMap<>(existing.size());
        for (GithubRepositoryEntity entity : existing) {
            byRepoId.put(entity.getGithubRepoId(), entity);
        }

        // 3) 머지/신규 생성
        List<GithubRepositoryEntity> toSave = new ArrayList<>(dtos.size());
        for (GithubRepositoryUpsertDto dto : dtos) {
            GithubRepositoryEntity entity = byRepoId.get(dto.githubRepoId());
            if (entity == null) {
                // 신규
                entity = GithubRepositoryEntity.from(dto);
            } else {
                // 업데이트
                entity.merge(dto);
            }
            toSave.add(entity);
        }

        // 4) 저장
        List<GithubRepositoryEntity> saved =  githubRepositoryRepository.saveAll(toSave);

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
