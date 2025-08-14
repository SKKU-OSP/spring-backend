package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.dto.GithubRepositoryUpsertDto;

import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
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
    public void upsertRepos(List<GithubRepositoryUpsertDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        // 1) 입력 검증 + 키 수집
        List<Long> ids = dtos.stream()
                .map(GithubRepositoryUpsertDto::githubRepoId)
                .filter(Objects::nonNull)
                .toList();
        if (ids.isEmpty()) return;

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
        githubRepositoryRepository.saveAll(toSave);
    }


}
