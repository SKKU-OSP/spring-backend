package com.sosd.sosd_backend.entity.github;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GitHub 활동 점수 계산 결과를 저장하는 Entity
 *
 * updateScore.py의 점수 계산 로직을 Spring에서 수행한 결과를 저장합니다.
 * - 연도별로 사용자의 GitHub 활동을 점수화
 * - 최대 5점 (레포 점수 + 타인 레포 기여 + Star + Fork + PR/Issue)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "github_score")
public class GithubScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * GitHub 계정 ID
     */
    @Column(name = "github_id", nullable = false)
    private Long githubId;

    /**
     * 학번
     */
    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;

    /**
     * 점수 계산 연도
     */
    @Column(name = "year", nullable = false)
    private Integer year;

    // ===== 기본 통계 정보 =====

    @Column(name = "commit_count")
    private Integer commitCount;

    @Column(name = "pr_count")
    private Integer prCount;

    @Column(name = "issue_count")
    private Integer issueCount;

    @Column(name = "total_addition")
    private Integer totalAddition;

    @Column(name = "total_deletion")
    private Integer totalDeletion;

    // ===== 점수 계산 결과 =====

    /**
     * 코드 라인 점수: min(total_lines / 10000, 1)
     */
    @Column(name = "commit_line_score", precision = 10, scale = 3)
    private BigDecimal commitLineScore;

    /**
     * 커밋 개수 점수: min(commit_count / 50, 1)
     */
    @Column(name = "commit_cnt_score", precision = 10, scale = 3)
    private BigDecimal commitCntScore;

    /**
     * PR + Issue 점수: min((pr_count + issue_count) * 0.1, 0.7)
     */
    @Column(name = "pull_n_issue_score", precision = 10, scale = 3)
    private BigDecimal pullNIssueScore;

    /**
     * README + License + Description 점수 (0.3)
     */
    @Column(name = "guideline_score", precision = 10, scale = 3)
    private BigDecimal guidelineScore;

    // ===== 자기 레포 점수 =====

    @Column(name = "repo_score_sub", precision = 10, scale = 3)
    private BigDecimal repoScoreSub;

    @Column(name = "repo_score_add", precision = 10, scale = 3)
    private BigDecimal repoScoreAdd;

    @Column(name = "repo_score_sum", precision = 10, scale = 3)
    private BigDecimal repoScoreSum;

    // ===== 타인 레포 기여 점수 =====

    @Column(name = "score_other_repo_sub", precision = 10, scale = 3)
    private BigDecimal scoreOtherRepoSub;

    @Column(name = "score_other_repo_add", precision = 10, scale = 3)
    private BigDecimal scoreOtherRepoAdd;

    @Column(name = "score_other_repo_sum", precision = 10, scale = 3)
    private BigDecimal scoreOtherRepoSum;

    // ===== Star/Fork 점수 =====

    /**
     * Star 점수: min(log10((star + 1.1) / 3), 2)
     */
    @Column(name = "score_star", precision = 10, scale = 3)
    private BigDecimal scoreStar;

    /**
     * Fork 점수: min(fork * 0.1, 1.0)
     */
    @Column(name = "score_fork", precision = 10, scale = 3)
    private BigDecimal scoreFork;

    // ===== 최종 점수 =====

    /**
     * 총점 (최대 5점)
     * = repo_score_sum + score_other_repo_sum + score_star + score_fork
     */
    @Column(name = "total_score", precision = 10, scale = 3)
    private BigDecimal totalScore;

    // ===== 메타 정보 =====

    /**
     * 최고 점수를 받은 레포지토리
     */
    @Column(name = "best_repo", length = 512)
    private String bestRepo;

    /**
     * 마지막 계산 시간
     */
    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    public GithubScoreEntity(
            Long id,
            Long githubId,
            String studentId,
            Integer year,
            Integer commitCount,
            Integer prCount,
            Integer issueCount,
            Integer totalAddition,
            Integer totalDeletion,
            BigDecimal commitLineScore,
            BigDecimal commitCntScore,
            BigDecimal pullNIssueScore,
            BigDecimal guidelineScore,
            BigDecimal repoScoreSub,
            BigDecimal repoScoreAdd,
            BigDecimal repoScoreSum,
            BigDecimal scoreOtherRepoSub,
            BigDecimal scoreOtherRepoAdd,
            BigDecimal scoreOtherRepoSum,
            BigDecimal scoreStar,
            BigDecimal scoreFork,
            BigDecimal totalScore,
            String bestRepo,
            LocalDateTime lastCalculatedAt
    ) {
        this.id = id;
        this.githubId = githubId;
        this.studentId = studentId;
        this.year = year;
        this.commitCount = commitCount;
        this.prCount = prCount;
        this.issueCount = issueCount;
        this.totalAddition = totalAddition;
        this.totalDeletion = totalDeletion;
        this.commitLineScore = commitLineScore;
        this.commitCntScore = commitCntScore;
        this.pullNIssueScore = pullNIssueScore;
        this.guidelineScore = guidelineScore;
        this.repoScoreSub = repoScoreSub;
        this.repoScoreAdd = repoScoreAdd;
        this.repoScoreSum = repoScoreSum;
        this.scoreOtherRepoSub = scoreOtherRepoSub;
        this.scoreOtherRepoAdd = scoreOtherRepoAdd;
        this.scoreOtherRepoSum = scoreOtherRepoSum;
        this.scoreStar = scoreStar;
        this.scoreFork = scoreFork;
        this.totalScore = totalScore;
        this.bestRepo = bestRepo;
        this.lastCalculatedAt = lastCalculatedAt;
    }

    /**
     * 신규 점수 엔티티 생성 정적 팩토리 메서드
     */
    public static GithubScoreEntity create(
            Long githubId,
            String studentId,
            Integer year
    ) {
        return GithubScoreEntity.builder()
                .githubId(githubId)
                .studentId(studentId)
                .year(year)
                .commitCount(0)
                .prCount(0)
                .issueCount(0)
                .totalAddition(0)
                .totalDeletion(0)
                .commitLineScore(BigDecimal.ZERO)
                .commitCntScore(BigDecimal.ZERO)
                .pullNIssueScore(BigDecimal.ZERO)
                .guidelineScore(BigDecimal.ZERO)
                .repoScoreSub(BigDecimal.ZERO)
                .repoScoreAdd(BigDecimal.ZERO)
                .repoScoreSum(BigDecimal.ZERO)
                .scoreOtherRepoSub(BigDecimal.ZERO)
                .scoreOtherRepoAdd(BigDecimal.ZERO)
                .scoreOtherRepoSum(BigDecimal.ZERO)
                .scoreStar(BigDecimal.ZERO)
                .scoreFork(BigDecimal.ZERO)
                .totalScore(BigDecimal.ZERO)
                .lastCalculatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 점수 업데이트 메서드
     * ScoreCalculationService에서 계산한 결과를 저장
     */
    public void updateScores(
            Integer commitCount,
            Integer prCount,
            Integer issueCount,
            Integer totalAddition,
            Integer totalDeletion,
            BigDecimal commitLineScore,
            BigDecimal commitCntScore,
            BigDecimal pullNIssueScore,
            BigDecimal guidelineScore,
            BigDecimal repoScoreSub,
            BigDecimal repoScoreAdd,
            BigDecimal repoScoreSum,
            BigDecimal scoreOtherRepoSub,
            BigDecimal scoreOtherRepoAdd,
            BigDecimal scoreOtherRepoSum,
            BigDecimal scoreStar,
            BigDecimal scoreFork,
            BigDecimal totalScore,
            String bestRepo
    ) {
        this.commitCount = commitCount;
        this.prCount = prCount;
        this.issueCount = issueCount;
        this.totalAddition = totalAddition;
        this.totalDeletion = totalDeletion;
        this.commitLineScore = commitLineScore;
        this.commitCntScore = commitCntScore;
        this.pullNIssueScore = pullNIssueScore;
        this.guidelineScore = guidelineScore;
        this.repoScoreSub = repoScoreSub;
        this.repoScoreAdd = repoScoreAdd;
        this.repoScoreSum = repoScoreSum;
        this.scoreOtherRepoSub = scoreOtherRepoSub;
        this.scoreOtherRepoAdd = scoreOtherRepoAdd;
        this.scoreOtherRepoSum = scoreOtherRepoSum;
        this.scoreStar = scoreStar;
        this.scoreFork = scoreFork;
        this.totalScore = totalScore;
        this.bestRepo = bestRepo;
        this.lastCalculatedAt = LocalDateTime.now();
    }
}
