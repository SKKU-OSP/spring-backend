package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryEntity;
import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryId;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.repository.github.AccountRepoLinkRepository;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubAccountRepositoryLinkServiceTest {

    @Mock AccountRepoLinkRepository linkRepo;
    @Mock GithubAccountRepository accountRepo;
    @Mock GithubRepositoryRepository repoRepo;

    @InjectMocks GithubAccountRepositoryLinkService service;

    private final Long accountId = 100L;
    private final Long repoId = 200L;

    // ---------- linkIfAbsent ----------

    @Test
    void linkIfAbsent_creates_whenNotExists() {
        // given
        var id = new GithubAccountRepositoryId(accountId, repoId);
        when(linkRepo.findById(id)).thenReturn(Optional.empty());

        var accountRef = mock(GithubAccount.class);
        var repoRef    = mock(GithubRepositoryEntity.class);
        when(accountRepo.getReferenceById(accountId)).thenReturn(accountRef);
        when(repoRepo.getReferenceById(repoId)).thenReturn(repoRef);

        var saved = GithubAccountRepositoryEntity.builder()
                .githubAccount(accountRef)
                .repository(repoRef)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
        when(linkRepo.save(any(GithubAccountRepositoryEntity.class))).thenReturn(saved);

        // when
        var result = service.linkIfAbsent(accountId, repoId);

        // then
        assertThat(result).isNotNull();
        verify(linkRepo).findById(id);
        verify(accountRepo).getReferenceById(accountId);
        verify(repoRepo).getReferenceById(repoId);
        verify(linkRepo).save(any(GithubAccountRepositoryEntity.class));
        verifyNoMoreInteractions(linkRepo, accountRepo, repoRepo);
    }

    @Test
    void linkIfAbsent_returnsExisting_whenExists() {
        // given
        var id = new GithubAccountRepositoryId(accountId, repoId);
        var existing = mock(GithubAccountRepositoryEntity.class);
        when(linkRepo.findById(id)).thenReturn(Optional.of(existing));

        // when
        var result = service.linkIfAbsent(accountId, repoId);

        // then
        assertThat(result).isSameAs(existing);
        verify(linkRepo).findById(id);
        verifyNoMoreInteractions(linkRepo, accountRepo, repoRepo);
    }

    // ---------- listRepos / listLinksWithRepo ----------

    @Test
    void listRepos_returnsJoinedRepos() {
        var r1 = mock(GithubRepositoryEntity.class);
        var r2 = mock(GithubRepositoryEntity.class);
        when(linkRepo.findReposByAccountId(accountId)).thenReturn(List.of(r1, r2));

        var result = service.listRepos(accountId);

        assertThat(result).containsExactly(r1, r2);
        verify(linkRepo).findReposByAccountId(accountId);
        verifyNoMoreInteractions(linkRepo);
    }

    @Test
    void listLinksWithRepo_returnsEmpty_whenInputEmpty() {
        var result = service.listLinksWithRepo(List.of());
        assertThat(result).isEmpty();
        verifyNoInteractions(linkRepo);
    }

    @Test
    void listLinksWithRepo_fetchJoin_whenIdsProvided() {
        var ids = Set.of(1L, 2L, 3L);
        var link = mock(GithubAccountRepositoryEntity.class);
        when(linkRepo.findAllByAccountIdsJoinRepo(ids)).thenReturn(List.of(link));

        var result = service.listLinksWithRepo(ids);

        assertThat(result).containsExactly(link);
        verify(linkRepo).findAllByAccountIdsJoinRepo(ids);
        verifyNoMoreInteractions(linkRepo);
    }

    // ---------- cursor updates ----------

    @Test
    void updateCommitCursor_createsLinkIfAbsent_thenDelegates() {
        stubLinkExists(); // 존재 가정. 미존재 케이스를 확인하려면 아래 테스트 참고

        service.updateCommitCursor(accountId, repoId, "sha123");

        verify(linkRepo).findById(new GithubAccountRepositoryId(accountId, repoId));
        verify(linkRepo).updateLastCommitSha(accountId, repoId, "sha123");
        verifyNoMoreInteractions(linkRepo);
    }

    @Test
    void updateCommitCursor_createsWhenMissing() {
        // 링크가 없으면 생성 흐름 타는지 검증
        var id = new GithubAccountRepositoryId(accountId, repoId);
        when(linkRepo.findById(id)).thenReturn(Optional.empty());

        var accountRef = mock(GithubAccount.class);
        var repoRef    = mock(GithubRepositoryEntity.class);
        when(accountRepo.getReferenceById(accountId)).thenReturn(accountRef);
        when(repoRepo.getReferenceById(repoId)).thenReturn(repoRef);
        when(linkRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateCommitCursor(accountId, repoId, "sha999");

        verify(linkRepo).findById(id);
        verify(accountRepo).getReferenceById(accountId);
        verify(repoRepo).getReferenceById(repoId);
        verify(linkRepo).save(any(GithubAccountRepositoryEntity.class));
        verify(linkRepo).updateLastCommitSha(accountId, repoId, "sha999");
        verifyNoMoreInteractions(linkRepo, accountRepo, repoRepo);
    }

    @Test
    void updatePrCursor_delegates() {
        stubLinkExists();
        var dt = LocalDateTime.now();

        service.updatePrCursor(accountId, repoId, dt);

        verify(linkRepo).updateLastPrDate(accountId, repoId, dt);
    }

    @Test
    void updateIssueCursor_delegates() {
        stubLinkExists();
        var dt = LocalDateTime.now();

        service.updateIssueCursor(accountId, repoId, dt);

        verify(linkRepo).updateLastIssueDate(accountId, repoId, dt);
    }

    @Test
    void touchUpdatedAt_delegates() {
        stubLinkExists();

        service.touchUpdatedAt(accountId, repoId);

        verify(linkRepo).touchLastUpdatedAt(accountId, repoId, any(LocalDateTime.class));
    }

    // ---------- helpers ----------

    private void stubLinkExists() {
        var id = new GithubAccountRepositoryId(accountId, repoId);
        var existing = mock(GithubAccountRepositoryEntity.class);
        when(linkRepo.findById(id)).thenReturn(Optional.of(existing));
    }
}
