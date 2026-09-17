package com.sosd.sosd_backend.entity.github;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class GithubRepositoryAvailabilityTest {

    @Test
    void repeatedNotFoundIsRequiredBeforeRepositoryIsExcluded() {
        GithubRepositoryEntity repository = repository();
        LocalDateTime checkedAt = LocalDateTime.of(2026, 9, 5, 12, 0);

        repository.markPubliclyUnavailable(checkedAt, 3, "Could not resolve to a Repository");
        assertThat(repository.getAvailabilityStatus())
                .isEqualTo(RepositoryAvailabilityStatus.SUSPECTED_UNAVAILABLE);
        assertThat(repository.getConsecutiveUnavailableCount()).isEqualTo(1);

        repository.markPubliclyUnavailable(checkedAt.plusHours(1), 3, "NOT_FOUND");
        assertThat(repository.getAvailabilityStatus())
                .isEqualTo(RepositoryAvailabilityStatus.SUSPECTED_UNAVAILABLE);

        repository.markPubliclyUnavailable(checkedAt.plusHours(2), 3, "NOT_FOUND");
        assertThat(repository.getAvailabilityStatus())
                .isEqualTo(RepositoryAvailabilityStatus.PUBLICLY_UNAVAILABLE);
        assertThat(repository.getConsecutiveUnavailableCount()).isEqualTo(3);
    }

    @Test
    void successfulCheckRestoresRepositoryAndUpdatesCanonicalName() {
        GithubRepositoryEntity repository = repository();
        LocalDateTime checkedAt = LocalDateTime.of(2026, 9, 5, 12, 0);
        repository.markPubliclyUnavailable(checkedAt, 1, "NOT_FOUND");

        repository.markAvailable("new-owner", "new-name", false, checkedAt.plusDays(1));

        assertThat(repository.getAvailabilityStatus()).isEqualTo(RepositoryAvailabilityStatus.AVAILABLE);
        assertThat(repository.getConsecutiveUnavailableCount()).isZero();
        assertThat(repository.getUnavailableSince()).isNull();
        assertThat(repository.getLastAvailabilityError()).isNull();
        assertThat(repository.getOwnerName()).isEqualTo("new-owner");
        assertThat(repository.getRepoName()).isEqualTo("new-name");
    }

    private GithubRepositoryEntity repository() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 1, 0, 0);
        return GithubRepositoryEntity.builder()
                .githubRepoId(1L)
                .ownerName("owner")
                .repoName("repo")
                .defaultBranch("main")
                .githubRepositoryCreatedAt(now)
                .githubRepositoryUpdatedAt(now)
                .githubPushedAt(now)
                .build();
    }
}
