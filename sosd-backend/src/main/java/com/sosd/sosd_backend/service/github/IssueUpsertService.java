package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.dto.github.GithubIssueUpsertDto;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.github.GithubIssueEntity;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.github_collector.dto.response.GithubIssueResponseDto;
import com.sosd.sosd_backend.github_collector.mapper.IssueMapper;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.repository.github.GithubIssueRepository;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueUpsertService {
    private final IssueMapper issueMapper;
    private final GithubIssueRepository githubIssueRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final GithubAccountRepository githubAccountRepository;

    /**
     * 단일 레포, 단일 계정에 대한 이슈들 upsert
     */
    @Transactional
    public void upsertSingleTargetFromResponses(
            Long repositoryId,
            Long accountId,
            List<GithubIssueResponseDto> responses
    ) {
        if (responses == null || responses.isEmpty()) return;

        // 1) 응답 -> UpsertDto 변환
        List<GithubIssueUpsertDto> dtos = responses.stream()
                .map(r -> issueMapper.toUpsertDto(r, repositoryId, accountId))
                .toList();

        // 2) DB upsert
        upsertSingleTargetFromDtos(repositoryId, accountId, dtos);
    }

    @Transactional
    public void upsertSingleTargetFromDtos(
            Long repositoryId,
            Long accountId,
            List<GithubIssueUpsertDto> dtos
    ) {
        if (dtos == null || dtos.isEmpty()) return;

        // 0) 모두 같은 타겟인지 검증
        boolean allMatch = dtos.stream()
                .allMatch(d -> Objects.equals(d.repositoryId(), repositoryId)
                        && Objects.equals(d.accountGithubId(), accountId));
        if (!allMatch) {
            throw new IllegalArgumentException("DTOs contain mismatched repositoryId or accountGithubId");
        }

        // 1) FK 준비 (프록시)
        GithubRepositoryEntity repository = githubRepositoryRepository.getReferenceById(repositoryId);
        GithubAccount account = githubAccountRepository.getReferenceById(accountId);

        // 2) 동일 githubIssueId에 대한 마지막 DTO로 덮어쓰기 (입력 순서 보존)
        Map<Long, GithubIssueUpsertDto> dtoMap = new LinkedHashMap<>();
        for (GithubIssueUpsertDto d : dtos) {
            dtoMap.put(d.githubIssueId(), d);
        }

        // 3) 기존 이슈 벌크 조회 (repo 범위 내 githubIssueId IN)
        Set<Long> issueIdSet = dtoMap.keySet();
        Map<Long, GithubIssueEntity> existedByIssueId = githubIssueRepository
                .findAllByRepository_IdAndGithubIssueIdIn(repositoryId, issueIdSet)
                .stream()
                .collect(Collectors.toMap(GithubIssueEntity::getGithubIssueId, e -> e));

        // 4) 신규/기존 분기
        List<GithubIssueEntity> toInsert = new ArrayList<>(dtoMap.size());
        for (GithubIssueUpsertDto d : dtoMap.values()) {
            GithubIssueEntity cur = existedByIssueId.get(d.githubIssueId());
            if (cur == null) {
                // 신규
                GithubIssueEntity e = GithubIssueEntity.create(d, repository, account);
                toInsert.add(e);
            } else {
                // 기존 → 부분 갱신
                cur.applyUpsert(d);
            }
        }

        // 5) 저장 (신규만)
        if (!toInsert.isEmpty()) {
            githubIssueRepository.saveAll(toInsert);
        }

    }

}
