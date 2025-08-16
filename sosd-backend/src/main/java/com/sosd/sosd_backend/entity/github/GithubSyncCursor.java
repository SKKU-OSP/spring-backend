package com.sosd.sosd_backend.entity.github;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@IdClass(GithubSyncCursor.CursorId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "github_sync_cursors")
public class GithubSyncCursor {

    @Id
    @Column(name = "github_account_id", nullable = false)
    private Long githubAccountId;

    @Id
    @Column(name = "github_repo_id", nullable = false)
    private Long githubRepoId;

    @Id
    @Column(name = "resource_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Column(name = "last_processed_sha", length = 40)
    private String lastProcessedSha;

    @Column(name = "last_processed_at")
    private LocalDateTime lastProcessedAt;

    @UpdateTimestamp
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    // 비즈니스 메서드
    public void updateCommitCursor(String newSha) {
        this.lastProcessedSha = newSha;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void updateTimeCursor(LocalDateTime newTime) {
        this.lastProcessedAt = newTime;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public boolean hasNeverSynced() {
        return this.lastProcessedSha == null && this.lastProcessedAt == null;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CursorId implements Serializable {
        private Long GithubAccountId;
        private Long GithubRepoId;
        private ResourceType resourceType;
    }

    @Getter
    @RequiredArgsConstructor
    public enum ResourceType {
        COMMIT("commit"),
        ISSUE("issue"),
        PR("pr"),
        STAR("star");

        private final String value;
    }
}


