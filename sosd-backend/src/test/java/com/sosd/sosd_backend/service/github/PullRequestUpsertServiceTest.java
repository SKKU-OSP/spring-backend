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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PullRequestUpsertServiceTest {

    @Mock PullRequestMapper prMapper;
    @Mock GithubPullRequestRepository prRepo;
    @Mock GithubRepositoryRepository repoRepo;
    @Mock GithubAccountRepository accountRepo;

    @InjectMocks PullRequestUpsertService service;

    @Test
    void upsertSingleTargetFromResponses_insertsNewAndUpdatesExisting() {
        // given
        Long repoId = 10L;
        Long accountId = 20L;

        var now = OffsetDateTime.now(ZoneOffset.UTC);

        // 응답 DTO (필드 구성은 매퍼가 mock이므로 실제 값 중요치 않음)
        var resp1 = new GithubPullRequestResponseDto(111L, 1, "title-1", "body-1", "open", now.minusDays(2) );
        var resp2 = new GithubPullRequestResponseDto(222L, 2, "title-2", "body-2", "closed",  now.minusDays(1) );

        // 매퍼 스텁: 응답 -> 업서트 DTO
        var dto1 = new GithubPullRequestUpsertDto(
                111L, 1, "title-1", "body-1",
                now.minusDays(2).toLocalDateTime(), true,
                repoId, accountId
        );
        var dto2 = new GithubPullRequestUpsertDto(
                222L, 2, "title-2", "body-2",
                now.minusDays(1).toLocalDateTime(), false,
                repoId, accountId
        );
        when(prMapper.toUpsertDto(resp1, repoId, accountId)).thenReturn(dto1);
        when(prMapper.toUpsertDto(resp2, repoId, accountId)).thenReturn(dto2);

        // FK 프록시
        var repoRef = mock(GithubRepositoryEntity.class);
        var accountRef = mock(GithubAccount.class);
        when(repoRepo.getReferenceById(repoId)).thenReturn(repoRef);
        when(accountRepo.getReferenceById(accountId)).thenReturn(accountRef);

        // 기존 PR: githubPrId=111L 존재, 222L 신규
        var existing = GithubPullRequestEntity.create(dto1, repoRef, accountRef);
        when(prRepo.findAllByRepository_IdAndGithubPrIdIn(
                eq(repoId),
                argThat(set -> set.containsAll(Set.of(111L, 222L)))
        )).thenReturn(List.of(existing));

        // when
        service.upsertSingleTargetFromResponses(repoId, accountId, List.of(resp1, resp2));

        // then
        ArgumentCaptor<List<GithubPullRequestEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(prRepo, times(1)).saveAll(captor.capture());
        var saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getGithubPrId()).isEqualTo(222L);

        verify(prRepo, times(1)).findAllByRepository_IdAndGithubPrIdIn(eq(repoId), any());
        verifyNoMoreInteractions(prRepo);
    }

}
