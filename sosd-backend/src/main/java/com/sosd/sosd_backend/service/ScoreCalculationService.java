package com.sosd.sosd_backend.service;

import com.sosd.sosd_backend.entity.github.GithubScoreEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * GitHub 활동 점수 계산 서비스
 *
 * OSP의 updateScore.py 로직을 Java로 구현
 * - 커밋 라인 수, 커밋 개수, PR/Issue 개수 등을 기반으로 점수 계산
 * - 최대 5점: repo_score_sum + score_other_repo_sum + score_star + score_fork
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreCalculationService {

    /**
     * 사용자의 연도별 GitHub 점수를 계산합니다.
     *
     * @param githubId 사용자 GitHub ID
     * @param studentId 학번
     * @param year 계산할 연도
     * @return 계산된 점수 엔티티
     */
    @Transactional
    public GithubScoreEntity calculateScore(Long githubId, String studentId, Integer year) {
        log.info("점수 계산 시작: githubId={}, studentId={}, year={}", githubId, studentId, year);

        // TODO: 실제 데이터베이스에서 조회
        // 현재는 더미 데이터로 테스트
        ScoreCalculationData data = fetchDataFromDatabase(githubId, year);

        // 1. 커밋 라인 점수 계산: min(total_lines / 10000, 1)
        int totalLines = data.totalAddition + data.totalDeletion;
        BigDecimal commitLineScore = BigDecimal.valueOf(totalLines)
                .divide(BigDecimal.valueOf(10000), 3, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE);

        // 2. 커밋 개수 점수 계산: min(commit_count / 50, 1)
        BigDecimal commitCntScore = BigDecimal.valueOf(data.commitCount)
                .divide(BigDecimal.valueOf(50), 3, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE);

        // 3. PR + Issue 점수 계산: min((pr_count + issue_count) * 0.1, 0.7)
        int prAndIssueCount = data.prCount + data.issueCount;
        BigDecimal pullNIssueScore = BigDecimal.valueOf(prAndIssueCount)
                .multiply(BigDecimal.valueOf(0.1))
                .min(BigDecimal.valueOf(0.7))
                .setScale(3, RoundingMode.HALF_UP);

        // 4. Guideline 점수: README + License + Description 있으면 0.3
        BigDecimal guidelineScore = data.hasReadme && data.hasLicense && data.hasDescription
                ? BigDecimal.valueOf(0.3)
                : BigDecimal.ZERO;

        // 5. 자기 레포 점수 계산
        BigDecimal repoScoreSub = commitLineScore; // 삭제 라인 기반
        BigDecimal repoScoreAdd = commitCntScore;   // 추가 라인 기반 (여기서는 커밋 개수 점수 사용)
        BigDecimal repoScoreSum = repoScoreSub
                .add(repoScoreAdd)
                .add(pullNIssueScore)
                .add(guidelineScore);

        // 6. 타인 레포 기여 점수 (현재는 0으로 설정, 나중에 구현)
        BigDecimal scoreOtherRepoSub = BigDecimal.ZERO;
        BigDecimal scoreOtherRepoAdd = BigDecimal.ZERO;
        BigDecimal scoreOtherRepoSum = BigDecimal.ZERO;

        // 7. Star 점수: min(log10((star + 1.1) / 3), 2)
        BigDecimal scoreStar = calculateStarScore(data.starCount);

        // 8. Fork 점수: min(fork * 0.1, 1.0)
        BigDecimal scoreFork = BigDecimal.valueOf(data.forkCount)
                .multiply(BigDecimal.valueOf(0.1))
                .min(BigDecimal.ONE)
                .setScale(3, RoundingMode.HALF_UP);

        // 9. 총점 계산 (최대 5점)
        BigDecimal totalScore = repoScoreSum
                .add(scoreOtherRepoSum)
                .add(scoreStar)
                .add(scoreFork)
                .min(BigDecimal.valueOf(5.0))
                .setScale(3, RoundingMode.HALF_UP);

        log.info("점수 계산 완료: githubId={}, totalScore={}", githubId, totalScore);

        // Entity 생성 또는 업데이트
        GithubScoreEntity scoreEntity = GithubScoreEntity.create(githubId, studentId, year);
        scoreEntity.updateScores(
                data.commitCount,
                data.prCount,
                data.issueCount,
                data.totalAddition,
                data.totalDeletion,
                commitLineScore,
                commitCntScore,
                pullNIssueScore,
                guidelineScore,
                repoScoreSub,
                repoScoreAdd,
                repoScoreSum,
                scoreOtherRepoSub,
                scoreOtherRepoAdd,
                scoreOtherRepoSum,
                scoreStar,
                scoreFork,
                totalScore,
                data.bestRepo
        );

        return scoreEntity;
    }

    /**
     * Star 점수 계산: min(log10((star + 1.1) / 3), 2)
     */
    private BigDecimal calculateStarScore(int starCount) {
        if (starCount <= 0) {
            return BigDecimal.ZERO;
        }

        // (star + 1.1) / 3
        double value = (starCount + 1.1) / 3.0;

        // log10 계산
        double logValue = Math.log10(value);

        // min(logValue, 2)
        BigDecimal score = BigDecimal.valueOf(Math.min(logValue, 2.0))
                .setScale(3, RoundingMode.HALF_UP);

        return score.max(BigDecimal.ZERO); // 음수 방지
    }

    /**
     * 데이터베이스에서 점수 계산에 필요한 데이터를 조회
     * TODO: 실제 Repository를 통해 데이터 조회 구현
     */
    private ScoreCalculationData fetchDataFromDatabase(Long githubId, Integer year) {
        // 현재는 더미 데이터 반환
        // 실제로는 JPA Repository를 사용해서 조회
        return ScoreCalculationData.builder()
                .commitCount(75)
                .prCount(5)
                .issueCount(3)
                .totalAddition(5000)
                .totalDeletion(2000)
                .starCount(150)
                .forkCount(25)
                .hasReadme(true)
                .hasLicense(true)
                .hasDescription(true)
                .bestRepo("donggyu/test-repo")
                .build();
    }

    /**
     * 점수 계산에 필요한 데이터를 담는 DTO
     */
    @lombok.Builder
    @lombok.Getter
    private static class ScoreCalculationData {
        private int commitCount;
        private int prCount;
        private int issueCount;
        private int totalAddition;
        private int totalDeletion;
        private int starCount;
        private int forkCount;
        private boolean hasReadme;
        private boolean hasLicense;
        private boolean hasDescription;
        private String bestRepo;
    }
}
