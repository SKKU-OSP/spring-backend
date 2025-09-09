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
class IssueUpsertServiceTest {

    @Mock IssueMapper issueMapper;
    @Mock GithubIssueRepository issueRepo;
    @Mock GithubRepositoryRepository repoRepo;
    @Mock GithubAccountRepository accountRepo;

    @InjectMocks IssueUpsertService service;

    @Test
    void upsertSingleTargetFromResponses_insertsNewAndUpdatesExisting() {
        // given
        Long repoId = 1000L;
        Long accountId = 2000L;

        var now = OffsetDateTime.now(ZoneOffset.UTC);

        var resp1 = new GithubIssueResponseDto(9001L, 101, "issue-1", "body-1", now.minusDays(3));
        var resp2 = new GithubIssueResponseDto(9002L, 102, "issue-2", "body-2", now.minusDays(2));

        var dto1 = new GithubIssueUpsertDto(
                9001L, 101, "issue-1", "body-1",
                now.minusDays(3).toLocalDateTime(),
                repoId, accountId
        );
        var dto2 = new GithubIssueUpsertDto(
                9002L, 102, "issue-2", "body-2",
                now.minusDays(2).toLocalDateTime(),
                repoId, accountId
        );

        when(issueMapper.toUpsertDto(resp1, repoId, accountId)).thenReturn(dto1);
        when(issueMapper.toUpsertDto(resp2, repoId, accountId)).thenReturn(dto2);

        var repoRef = mock(GithubRepositoryEntity.class);
        var accountRef = mock(GithubAccount.class);
        when(repoRepo.getReferenceById(repoId)).thenReturn(repoRef);
        when(accountRepo.getReferenceById(accountId)).thenReturn(accountRef);

        // 기존 이슈: 9001L 존재, 9002L 신규
        var existing = GithubIssueEntity.create(dto1, repoRef, accountRef);
        when(issueRepo.findAllByRepository_IdAndGithubIssueIdIn(
                eq(repoId),
                argThat(set -> set.containsAll(Set.of(9001L, 9002L)))
        )).thenReturn(List.of(existing));

        // when
        service.upsertSingleTargetFromResponses(repoId, accountId, List.of(resp1, resp2));

        // then
        ArgumentCaptor<List<GithubIssueEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(issueRepo, times(1)).saveAll(captor.capture());
        var saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getGithubIssueId()).isEqualTo(9002L);

        verify(issueRepo, times(1)).findAllByRepository_IdAndGithubIssueIdIn(eq(repoId), any());
        verifyNoMoreInteractions(issueRepo);
    }

    @Test
    void upsertSingleTargetFromResponses_noInput_doesNothing() {
        Long repoId = 1000L, accountId = 2000L;
        service.upsertSingleTargetFromResponses(repoId, accountId, Collections.emptyList());
        verifyNoInteractions(issueRepo, repoRepo, accountRepo, issueMapper);
    }
}
