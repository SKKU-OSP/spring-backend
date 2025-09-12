package com.sosd.sosd_backend.service.github;


import com.sosd.sosd_backend.dto.github.GithubCommitUpsertDto;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.github.GithubCommitEntity;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.github_collector.dto.response.GithubCommitResponseDto;
import com.sosd.sosd_backend.github_collector.mapper.CommitMapper;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.repository.github.GithubCommitRepository;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommitUpsertService {

    private final CommitMapper commitMapper;
    private final GithubCommitRepository githubCommitRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final GithubAccountRepository githubAccountRepository;

    /**
     * 단일 레포, 단일 계정에 대한 커밋들 upsert
     * 현재 서비스에서는 단일 레포, 단일 계정 단위로 커밋을 수집하기 때무에 이 메서드만 구현
     */
    @Transactional
    public void upsertSingleTargetFromResponses(
            Long repositoryId,
            Long accountId,
            List<GithubCommitResponseDto> responses
    ) {
        if (responses == null || responses.isEmpty()) return;

        // 1. 응답 -> UpsertDto 변환
        List<GithubCommitUpsertDto> dtos = responses.stream()
                .map(r -> commitMapper.toUpsertDto(r, repositoryId, accountId))
                .toList();

        // 2. DB에 Upsert
        upsertSingleTargetFromDtos(repositoryId, accountId, dtos);

    }

    @Transactional
    public void upsertSingleTargetFromDtos(
            Long repositoryId,
            Long accountId,
            List<GithubCommitUpsertDto> dtos
    ) {
        if (dtos == null || dtos.isEmpty()) return;

        // 0. 모두 같은 repoId, accountId target인지 검증
        boolean allMatch = dtos.stream()
                .allMatch(d -> d.repositoryId().equals(repositoryId) && d.accountGithubId().equals(accountId));
        if (!allMatch) {
            throw new IllegalArgumentException("DTOs contain mismatched repositoryId or accountGithubId");
        }

        // 1. FK 준비: 프록시 조회
        GithubRepositoryEntity repository = githubRepositoryRepository.getReferenceById(repositoryId);
        GithubAccount account = githubAccountRepository.getReferenceById(accountId);

        // 2. 동일 sha 중복 덮어쓰기
        Map<String, GithubCommitUpsertDto> dtoMap = new LinkedHashMap<>();
        for(GithubCommitUpsertDto d : dtos) {
            dtoMap.put(d.sha(), d);
        }

        // 3. 기존 커밋 벌크 조회
        Set<String> shaSet = dtoMap.keySet();
        Map<String, GithubCommitEntity> existedBySha = githubCommitRepository
                .findAllByRepository_IdAndShaIn(repositoryId, shaSet)
                .stream()
                .collect(Collectors.toMap(GithubCommitEntity::getSha, e -> e));

        // 4. 신규/기존 분리
        List<GithubCommitEntity> toInsert = new ArrayList<>(dtoMap.size());
        for (var d : dtoMap.values()) {
            GithubCommitEntity cur = existedBySha.get(d.sha());
            if (cur == null) {
                // 신규
                GithubCommitEntity e = GithubCommitEntity.create(d, repository, account);
                toInsert.add(e);
            } else {
                // 기존 - 부분 갱신
                cur.applyUpsert(d);
            }
        }

        // 5. 저장
        if (!toInsert.isEmpty()) {
            githubCommitRepository.saveAll(toInsert);
        }

    }

}
