package com.sosd.sosd_backend.entity.github;


import com.sosd.sosd_backend.constant.CollectionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_account_repository")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubAccountRepositoryEntity {

    @EmbeddedId
    private GithubAccountRepositoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("githubAccountId")
    @JoinColumn(name = "github_account_id", referencedColumnName = "github_id")
    private GithubAccount githubAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("githubRepoId")      // 복합키의 repo 부분을 이 연관과 매핑
    @JoinColumn(name = "github_repo_id", referencedColumnName = "id")
    private GithubRepositoryEntity repository;

    @Column(name = "last_commit_sha", length = 40)
    private String lastCommitSha;

    @Column(name = "last_pr_date")
    private LocalDateTime lastPrDate;

    @Column(name = "last_issue_date")
    private LocalDateTime lastIssueDate;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "weight", nullable = false)
    private Integer weight = 1;

    @Column(name = "next_collect_date", nullable = false)
    private LocalDateTime nextCollectDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CollectionStatus status = CollectionStatus.READY;

    @Builder
    public GithubAccountRepositoryEntity(GithubAccount githubAccount,
                                         GithubRepositoryEntity repository,
                                         String lastCommitSha,
                                         LocalDateTime lastPrDate,
                                         LocalDateTime lastIssueDate,
                                         LocalDateTime lastUpdatedAt,
                                         Integer weight,
                                         LocalDateTime nextCollectDate,
                                         CollectionStatus status) {
        this.githubAccount = githubAccount;
        this.repository = repository;
        this.id = new GithubAccountRepositoryId(githubAccount.getGithubId(), repository.getId());
        this.lastCommitSha = lastCommitSha;
        this.lastPrDate = lastPrDate;
        this.lastIssueDate = lastIssueDate;
        this.lastUpdatedAt = lastUpdatedAt;
        if (weight != null) this.weight = weight;
        if (nextCollectDate != null) this.nextCollectDate = nextCollectDate;
        if (status != null) this.status = status;
    }

    // --- Update Methods --- //
    public void touchUpdatedAt() { this.lastUpdatedAt = LocalDateTime.now(); }
    public void updateCommitCursor(String sha) { this.lastCommitSha = sha; touchUpdatedAt(); }
    public void updatePrCursor(LocalDateTime dt) { this.lastPrDate = dt; touchUpdatedAt(); }
    public void updateIssueCursor(LocalDateTime dt) { this.lastIssueDate = dt; touchUpdatedAt(); }

    // --- 스케줄링 관련 메서드 --- //

    // 스케줄링 큐에 넣을 때 호출
    public void markAsQueued() { this.status = CollectionStatus.QUEUED; }
    public void markAsNameRescueNeeded() { this.status = CollectionStatus.NAME_RESCUE; }
    public void markAsDiverged() { this.status = CollectionStatus.DIVERGED; }
    public void forceResetToReady() { this.status = CollectionStatus.READY; }

    // 스케줄링 작업이 끝난 후 다음 수집 일시와 상태를 업데이트
    public void updateScheduleInfo(int newWeight, LocalDateTime nextTime) {
        this.weight = newWeight;
        this.nextCollectDate = nextTime;
        this.status = CollectionStatus.READY; // 다시 수집 가능 상태로 복귀
        touchUpdatedAt();
    }

    // 수집을 연기할 때 호출
    public void deferSchedule(LocalDateTime resetTime) {
        this.nextCollectDate = resetTime;
        this.status = CollectionStatus.READY;
    }

}
