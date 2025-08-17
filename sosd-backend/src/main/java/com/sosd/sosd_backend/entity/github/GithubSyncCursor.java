package com.sosd.sosd_backend.entity.github;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "github_sync_cursors")
public class GithubSyncCursor {

    // 복합 PK키
    @EmbeddedId
    private CursorId id;

    @Column(name = "last_processed_sha", length = 40)
    private String lastProcessedSha;

    @Column(name = "last_processed_at")
    private LocalDateTime lastProcessedAt;

    @UpdateTimestamp
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Builder
    private GithubSyncCursor(Long githubAccountId,
                             Long githubRepoId,
                             ResourceType resourceType,
                             String lastProcessedSha,
                             LocalDateTime lastProcessedAt) {
        this.id = new CursorId(githubAccountId, githubRepoId, resourceType);
        this.lastProcessedSha = lastProcessedSha;
        this.lastProcessedAt = lastProcessedAt;
    }

    // 편의 접근자
    public Long getGithubAccountId() { return id.githubAccountId; }
    public Long getGithubRepoId() { return id.githubRepoId; }
    public ResourceType getResourceType() { return id.resourceType; }

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
    @Embeddable
    public static class CursorId implements Serializable {
        @Column(name = "github_account_id", nullable = false)
        private Long githubAccountId;

        @Column(name = "github_repo_id", nullable = false)
        private Long githubRepoId;

        @Enumerated(EnumType.STRING) // DB ENUM('COMMIT','ISSUE','PR','STAR')와 1:1 매핑
        @Column(name = "resource_type", nullable = false)
        private ResourceType resourceType;
    }

    @Getter
    public enum ResourceType {
        COMMIT, ISSUE, PR, STAR
    }
}