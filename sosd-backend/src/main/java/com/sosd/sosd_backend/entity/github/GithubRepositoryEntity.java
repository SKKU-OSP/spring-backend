package com.sosd.sosd_backend.entity.github;


import com.sosd.sosd_backend.dto.github.GithubRepositoryUpsertDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "github_repository")
public class GithubRepositoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "github_repo_id", nullable = false)
    private Long githubRepoId;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "repo_name", nullable = false)
    private String repoName;

    @Column(name = "full_name", insertable = false, updatable = false)
    private String fullName;

    @Column(name = "default_branch", nullable = false)
    private String defaultBranch;

    @Column(name = "watcher")
    private Integer watcher;

    @Column(name = "star")
    private Integer star;

    @Column(name = "fork")
    private Integer fork;

    @Column(name = "open_pr")
    private Integer openPr;

    @Column(name = "closed_pr")
    private Integer closedPr;

    @Column(name = "merged_pr")
    private Integer mergedPr;

    @Column(name = "open_issue")
    private Integer openIssue;

    @Column(name = "closed_issue")
    private Integer closedIssue;

    @Column(name = "`commit`")
    private Integer commit;

    @Column(name = "dependency")
    private Integer dependency;

    @Column(name = "language")
    private String language;

    @Column(name = "release_ver")
    private String releaseVer;

    @Column(name = "release_count")
    private Integer releaseCount;

    @Column(name = "description")
    private String description;

    @Lob
    @Column(name = "readme", columnDefinition = "MEDIUMTEXT")
    private String readme;

    @Column(name = "license")
    private String license;

    @Column(name = "github_repository_created_at", nullable = false)
    private LocalDateTime githubRepositoryCreatedAt;

    @Column(name = "github_repository_updated_at", nullable = false)
    private LocalDateTime githubRepositoryUpdatedAt;

    @Column(name = "github_pushed_at", nullable = false)
    private LocalDateTime githubPushedAt;

    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;

    @Column(name = "contributor")
    private Integer contributor;

    @Column(name = "is_private")
    private Boolean isPrivate;

    @Column(name = "last_starred_at")
    private LocalDateTime lastStarredAt;

    @Column(name = "last_collected_at")
    private LocalDateTime lastCollectedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 32)
    private RepositoryAvailabilityStatus availabilityStatus = RepositoryAvailabilityStatus.AVAILABLE;

    @Column(name = "consecutive_unavailable_count", nullable = false)
    private Integer consecutiveUnavailableCount = 0;

    @Column(name = "last_availability_checked_at")
    private LocalDateTime lastAvailabilityCheckedAt;

    @Column(name = "unavailable_since")
    private LocalDateTime unavailableSince;

    @Column(name = "last_availability_error", length = 512)
    private String lastAvailabilityError;

    @Builder
    public GithubRepositoryEntity(
            Long githubRepoId,
            String ownerName,
            String repoName,
            String defaultBranch,
            LocalDateTime githubRepositoryCreatedAt,
            LocalDateTime githubRepositoryUpdatedAt,
            LocalDateTime githubPushedAt
    ) {
        this.githubRepoId = githubRepoId;
        this.ownerName = ownerName;
        this.repoName = repoName;
        this.defaultBranch = defaultBranch;
        this.githubRepositoryCreatedAt = githubRepositoryCreatedAt;
        this.githubRepositoryUpdatedAt = githubRepositoryUpdatedAt;
        this.githubPushedAt = githubPushedAt;
    }


    public static GithubRepositoryEntity from(GithubRepositoryUpsertDto dto) {
        // null 체크
        if (dto.githubRepoId() == null) throw new IllegalArgumentException("Github repository id cannot be null");
        if (dto.ownerName() == null) throw new IllegalArgumentException("Owner name cannot be null");
        if (dto.repoName() == null) throw new IllegalArgumentException("Repo name cannot be null");
        if (dto.defaultBranch() == null) throw new IllegalArgumentException("Default branch cannot be null");
        if (dto.githubRepositoryUpdatedAt() == null) throw new IllegalArgumentException("Github repository updated at cannot be null");
        if (dto.githubRepositoryCreatedAt() == null) throw new IllegalArgumentException("Github repository created a cannot be null");
        if (dto.githubPushedAt() == null) throw new IllegalArgumentException("Github repository pushed at cannot be null");

        GithubRepositoryEntity e = GithubRepositoryEntity.builder()
                .githubRepoId(dto.githubRepoId())
                .ownerName(dto.ownerName())
                .repoName(dto.repoName())
                .defaultBranch(dto.defaultBranch())
                .githubRepositoryCreatedAt(dto.githubRepositoryCreatedAt())
                .githubRepositoryUpdatedAt(dto.githubRepositoryUpdatedAt())
                .githubPushedAt(dto.githubPushedAt())
                .build();
        e.merge(dto);
        return e;
    }

    /** 부분 업데이트: null/blank가 아닌 값만 반영. 식별자(githubRepoId)는 변경하지 않음. */
    public void merge(GithubRepositoryUpsertDto dto){
        if (dto.ownerName() != null) this.ownerName = dto.ownerName();
        if (dto.repoName() != null) this.repoName = dto.repoName();
        if (dto.defaultBranch() != null && !dto.defaultBranch().isBlank()) this.defaultBranch = dto.defaultBranch();
        if (dto.watcher() != null) this.watcher = dto.watcher();
        if (dto.star() != null) this.star = dto.star();
        if (dto.fork() != null) this.fork = dto.fork();
        if (dto.openPr() != null) this.openPr = dto.openPr();
        if (dto.closedPr() != null) this.closedPr = dto.closedPr();
        if (dto.mergedPr() != null) this.mergedPr = dto.mergedPr();
        if (dto.openIssue() != null) this.openIssue = dto.openIssue();
        if (dto.closedIssue() != null) this.closedIssue = dto.closedIssue();
        if (dto.commit() != null) this.commit = dto.commit();
        if (dto.dependency() != null) this.dependency = dto.dependency();
        if (dto.language() != null) this.language = dto.language();
        if (dto.releaseVer() != null) this.releaseVer = dto.releaseVer();
        if (dto.releaseCount() != null) this.releaseCount = dto.releaseCount();
        if (dto.description() != null) this.description = dto.description();
        if (dto.readme() != null) this.readme = dto.readme();
        if (dto.license() != null) this.license = dto.license();
        if (dto.githubRepositoryCreatedAt() != null) this.githubRepositoryCreatedAt = dto.githubRepositoryCreatedAt();
        if (dto.githubRepositoryUpdatedAt() != null) this.githubRepositoryUpdatedAt = dto.githubRepositoryUpdatedAt();
        if (dto.githubPushedAt() != null) this.githubPushedAt = dto.githubPushedAt();
        if (dto.additionalData() != null) this.additionalData = dto.additionalData();
        if (dto.contributor() != null) this.contributor = dto.contributor();
        if (dto.isPrivate() != null) {
            markAvailable(dto.ownerName(), dto.repoName(), dto.isPrivate(), LocalDateTime.now());
        }
    }

    /** GitHub에서 같은 repository id를 정상 조회한 경우 상태와 연속 실패 횟수를 복구한다. */
    public void markAvailable(String canonicalOwnerName, String canonicalRepoName,
                              Boolean privateRepository, LocalDateTime checkedAt) {
        if (canonicalOwnerName != null && !canonicalOwnerName.isBlank()) this.ownerName = canonicalOwnerName;
        if (canonicalRepoName != null && !canonicalRepoName.isBlank()) this.repoName = canonicalRepoName;
        if (privateRepository != null) this.isPrivate = privateRepository;
        this.availabilityStatus = RepositoryAvailabilityStatus.AVAILABLE;
        this.consecutiveUnavailableCount = 0;
        this.lastAvailabilityCheckedAt = checkedAt;
        this.unavailableSince = null;
        this.lastAvailabilityError = null;
    }

    /** 명시적인 NOT_FOUND만 누적한다. 일시적 API 오류는 이 메서드의 호출 대상이 아니다. */
    public void markPubliclyUnavailable(LocalDateTime checkedAt, int confirmationThreshold, String error) {
        int threshold = Math.max(1, confirmationThreshold);
        int failures = (consecutiveUnavailableCount == null ? 0 : consecutiveUnavailableCount) + 1;
        this.consecutiveUnavailableCount = failures;
        this.lastAvailabilityCheckedAt = checkedAt;
        if (this.unavailableSince == null) this.unavailableSince = checkedAt;
        this.lastAvailabilityError = abbreviate(error, 512);
        this.availabilityStatus = failures >= threshold
                ? RepositoryAvailabilityStatus.PUBLICLY_UNAVAILABLE
                : RepositoryAvailabilityStatus.SUSPECTED_UNAVAILABLE;
    }

    public boolean isPubliclyUnavailable() {
        return availabilityStatus == RepositoryAvailabilityStatus.PUBLICLY_UNAVAILABLE;
    }

    private static String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value;
        return value.substring(0, maxLength);
    }

}
