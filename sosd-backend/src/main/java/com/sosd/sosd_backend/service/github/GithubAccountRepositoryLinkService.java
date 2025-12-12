package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryEntity;
import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryId;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;
import com.sosd.sosd_backend.repository.github.AccountRepoLinkRepository;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GithubAccountRepositoryLinkService {

    private final AccountRepoLinkRepository linkRepo;
    private final GithubAccountRepository accountRepo;
    private final GithubRepositoryRepository repoRepo;

    /** 1) accountId-repoId 링크 없으면 생성 (Upsert) */
    @Transactional
    public GithubAccountRepositoryEntity linkIfAbsent(Long accountId, Long repoId) {
        var id = new GithubAccountRepositoryId(accountId, repoId);
        return linkRepo.findById(id).orElseGet(() -> {
            var accountRef = accountRepo.getReferenceById(accountId); // SELECT 회피
            var repoRef    = repoRepo.getReferenceById(repoId);
            var entity = GithubAccountRepositoryEntity.builder()
                    .githubAccount(accountRef)
                    .repository(repoRef)
                    .lastUpdatedAt(LocalDateTime.now())
                    .build();
            return linkRepo.save(entity);
        });
    }

    /** 2) 단일 계정의 레포 목록 (엔티티 필요 없으면 이거) */
    @Transactional(readOnly = true)
    public List<GithubRepositoryEntity> listRepos(Long accountId) {
        return linkRepo.findReposByAccountId(accountId);
    }

    /** 2-1) repoRef 반환 */
    @Transactional(readOnly = true)
    public List<RepoRef> listRepoRefs(Long accountId) {
        return linkRepo.findReposByAccountId(accountId).stream()
                .map(e -> new RepoRef(
                        e.getId(),
                        e.getGithubRepoId(),
                        e.getOwnerName(),
                        e.getRepoName(),
                        e.getFullName(),
                        e.getGithubRepositoryUpdatedAt(),
                        e.getGithubPushedAt()
                ))
                .toList();
    }

    /** 2-2) 여러 계정의 링크+레포를 한 번에 (fetch join) */
    @Transactional(readOnly = true)
    public List<GithubAccountRepositoryEntity> listLinksWithRepo(Collection<Long> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) return List.of();
        return linkRepo.findAllByAccountIdsJoinRepo(accountIds);
    }

    /** 3) 커서 가져오기 */
    @Transactional(readOnly = true)
    public Optional<String> getLastCommitSha(Long accountId, Long repoId) {
        return linkRepo.findLastCommitSha(accountId, repoId);
    }

    @Transactional(readOnly = true)
    public Optional<LocalDateTime> getLastPrDate(Long accountId, Long repoId) {
        return linkRepo.findLastPrDate(accountId, repoId);
    }

    @Transactional(readOnly = true)
    public Optional<LocalDateTime> getLastIssueDate(Long accountId, Long repoId) {
        return linkRepo.findLastIssueDate(accountId, repoId);
    }

    @Transactional(readOnly = true)
    public Optional<LocalDateTime> getLastUpdatedAt(Long accountId, Long repoId) {
        return linkRepo.findLastUpdatedAt(accountId, repoId);
    }

    /** 4) 커서 갱신들 */
    @Transactional
    public void updateCommitCursor(Long accountId, Long repoId, String sha) {
        linkIfAbsent(accountId, repoId); // 없으면 만들어두고
        linkRepo.updateLastCommitSha(accountId, repoId, sha);
    }

    @Transactional
    public void updatePrCursor(Long accountId, Long repoId, LocalDateTime dt) {
        linkIfAbsent(accountId, repoId);
        linkRepo.updateLastPrDate(accountId, repoId, dt);
    }

    @Transactional
    public void updateIssueCursor(Long accountId, Long repoId, LocalDateTime dt) {
        linkIfAbsent(accountId, repoId);
        linkRepo.updateLastIssueDate(accountId, repoId, dt);
    }

    @Transactional
    public void touchUpdatedAt(Long accountId, Long repoId) {
        linkIfAbsent(accountId, repoId);
        linkRepo.touchLastUpdatedAt(accountId, repoId, LocalDateTime.now());
    }

    // -- 5) 스케줄링 관련 메서드

    /**
     * 스케줄러가 큐에 넣을 때 호출 (Locking)
     * 상태를 QUEUED로 변경하여 다른 스케줄러가 중복 수집하지 못하게 함
     */
    @Transactional
    public void markAsQueued(Long accountId, Long repoId) {
        GithubAccountRepositoryEntity link = findEntityOrThrow(accountId, repoId);
        link.markAsQueued(); // Entity의 편의 메서드 호출
    }

    /**
     * 수집 성공 후 스케줄 업데이트 (MLFQ)
     * 가중치와 다음 수집 시간을 갱신하고 상태를 READY로 복귀
     */
    @Transactional
    public void updateScheduleInfo(Long accountId, Long repoId, int newWeight, LocalDateTime nextCollectDate) {
        GithubAccountRepositoryEntity link = findEntityOrThrow(accountId, repoId);
        link.updateScheduleInfo(newWeight, nextCollectDate);
    }

    /**
     * RateLimit 등으로 인한 지연 (Defer)
     * 상태는 READY로 두되, 시간만 뒤로 미룸
     */
    @Transactional
    public void deferSchedule(Long accountId, Long repoId, LocalDateTime deferredTime) {
        GithubAccountRepositoryEntity link = findEntityOrThrow(accountId, repoId);
        link.deferSchedule(deferredTime);
    }

    /**
     * 404 발생 시 (Name Rescue 요청)
     */
    @Transactional
    public void markAsNameRescueNeeded(Long accountId, Long repoId) {
        GithubAccountRepositoryEntity link = findEntityOrThrow(accountId, repoId);
        link.markAsNameRescueNeeded();
    }

    /**
     * 정합성 깨짐 발생 시 (Diverged)
     */
    @Transactional
    public void markAsDiverged(Long accountId, Long repoId) {
        GithubAccountRepositoryEntity link = findEntityOrThrow(accountId, repoId);
        link.markAsDiverged();
    }

    // 내부 헬퍼 메서드
    private GithubAccountRepositoryEntity findEntityOrThrow(Long accountId, Long repoId) {
        return linkRepo.findById(new GithubAccountRepositoryId(accountId, repoId))
                .orElseThrow(() -> new EntityNotFoundException("Link not found: " + accountId + "-" + repoId));
    }
}
