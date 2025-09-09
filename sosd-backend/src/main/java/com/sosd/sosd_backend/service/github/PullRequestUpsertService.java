package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.dto.github.GithubPullRequestUpsertDto;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.github.GithubPullRequestEntity;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.github_collector.dto.response.GithubPullRequestResponseDto;
import com.sosd.sosd_backend.github_collector.mapper.PullRequestMapper;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.repository.github.GithubPullRequestRepository;
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
public class PullRequestUpsertService {

    private final PullRequestMapper pullRequestMapper;
    private final GithubPullRequestRepository githubPullRequestRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final GithubAccountRepository githubAccountRepository;

    /**
     * 단일 레포, 단일 계정에 대한 PR들 upsert
     * 현재 수집 로직이 repo/account 단위이므로 이 메서드만 구현
     */
    @Transactional
    public void upsertSingleTargetFromResponses(
            Long repositoryId,
            Long accountId,
            List<GithubPullRequestResponseDto> responses
    ) {
        if (responses == null || responses.isEmpty()) return;

        // 1) 응답 -> UpsertDto 변환
        List<GithubPullRequestUpsertDto> dtos = responses.stream()
                .map(r -> pullRequestMapper.toUpsertDto(r, repositoryId, accountId))
                .toList();

        // 2) DB upsert
        upsertSingleTargetFromDtos(repositoryId, accountId, dtos);
    }

    @Transactional
    public void upsertSingleTargetFromDtos(
            Long repositoryId,
            Long accountId,
            List<GithubPullRequestUpsertDto> dtos
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

        // 2) 중복 githubPrId에 대해 마지막 값으로 덮어쓰기 (LinkedHashMap 유지)
        Map<Long, GithubPullRequestUpsertDto> dtoMap = new LinkedHashMap<>();
        for (GithubPullRequestUpsertDto d : dtos) {
            dtoMap.put(d.githubPrId(), d);
        }

        // 3) 기존 PR 벌크 조회 (repo 범위 내 githubPrId IN)
        Set<Long> prIdSet = dtoMap.keySet();
        Map<Long, GithubPullRequestEntity> existedByPrId = githubPullRequestRepository
                .findAllByRepository_IdAndGithubPrIdIn(repositoryId, prIdSet)
                .stream()
                .collect(Collectors.toMap(GithubPullRequestEntity::getGithubPrId, e -> e));

        // 4) 신규/기존 분기
        List<GithubPullRequestEntity> toInsert = new ArrayList<>(dtoMap.size());
        for (GithubPullRequestUpsertDto d : dtoMap.values()) {
            GithubPullRequestEntity cur = existedByPrId.get(d.githubPrId());
            if (cur == null) {
                // 신규
                GithubPullRequestEntity e = GithubPullRequestEntity.create(d, repository, account);
                toInsert.add(e);
            } else {
                // 기존 → 부분 갱신
                cur.applyUpsert(d);
            }
        }

        // 5) 저장
        if (!toInsert.isEmpty()) {
            githubPullRequestRepository.saveAll(toInsert);
        }

    }

}
