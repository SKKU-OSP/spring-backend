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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommitUpsertServiceTest {

    @Mock CommitMapper commitMapper;
    @Mock GithubCommitRepository commitRepo;
    @Mock GithubRepositoryRepository repoRepo;
    @Mock GithubAccountRepository accountRepo;

    @InjectMocks CommitUpsertService service;

    @Test
    void upsertSingleTargetFromResponses_insertsNewAndUpdatesExisting() {
        // given
        Long repoId = 100L;
        Long accountId = 200L; // github_id가 PK라고 가정

        var now = OffsetDateTime.now(ZoneOffset.UTC);
        var resp1 = new GithubCommitResponseDto("sha-1", 10, 2, "feat: A", now.minusDays(1), now.minusDays(1));
        var resp2 = new GithubCommitResponseDto("sha-2", 5, 1, "fix: B", now, now);

        // 매퍼 스텁: 응답 -> 업서트 DTO
        var dto1 = new GithubCommitUpsertDto("sha-1", 10, 2, now.minusDays(1).toLocalDateTime(), now.minusDays(1).toLocalDateTime(), "feat: A", repoId, accountId);
        var dto2 = new GithubCommitUpsertDto("sha-2", 5, 1, now.toLocalDateTime(), now.toLocalDateTime(), "fix: B", repoId, accountId);

        when(commitMapper.toUpsertDto(resp1, repoId, accountId)).thenReturn(dto1);
        when(commitMapper.toUpsertDto(resp2, repoId, accountId)).thenReturn(dto2);

        // FK 프록시 스텁
        var repoRef = mock(GithubRepositoryEntity.class);
        var accountRef = mock(GithubAccount.class);
        when(repoRepo.getReferenceById(repoId)).thenReturn(repoRef);
        when(accountRepo.getReferenceById(accountId)).thenReturn(accountRef);

        // 기존 커밋: sha-1은 이미 존재, sha-2는 신규
        var existing = GithubCommitEntity.create(dto1, repoRef, accountRef);
        when(commitRepo.findAllByRepository_IdAndShaIn(eq(repoId), argThat(set -> set.containsAll(Set.of("sha-1", "sha-2")))))
                .thenReturn(List.of(existing));

        // when
        service.upsertSingleTargetFromResponses(repoId, accountId, List.of(resp1, resp2));

        // then
        // saveAll은 신규 1건(sha-2)만 저장해야 한다
        ArgumentCaptor<List<GithubCommitEntity>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(commitRepo, times(1)).saveAll(savedCaptor.capture());
        var saved = savedCaptor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getSha()).isEqualTo("sha-2");

        // 기존 엔티티는 applyUpsert로 업데이트만 되었으므로 saveAll 대상에 없음
        verify(commitRepo, times(1)).findAllByRepository_IdAndShaIn(eq(repoId), any());
        verifyNoMoreInteractions(commitRepo);
    }
}
