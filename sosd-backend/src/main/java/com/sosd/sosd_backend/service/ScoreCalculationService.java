package com.sosd.sosd_backend.service;

import com.sosd.sosd_backend.data_aggregation.entity.GithubContributionStats;
import com.sosd.sosd_backend.entity.github.GithubScoreEntity;
import com.sosd.sosd_backend.data_aggregation.repository.AggregationGithubContributionStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * GitHub 활동 점수 계산 서비스
 *
 * OSP의 updateScore.py Ver1 로직을 Java로 구현
 * - 레포별로 점수를 계산 후 정렬
 * - best repo 점수 + 2nd/3rd repo 점수(최대 1.0) + star 점수 + fork 점수
 * - 최대 5점
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreCalculationService {

    private final AggregationGithubContributionStatsRepository statsRepository;

    /**
     * 사용자의 연도별 GitHub 점수를 계산합니다. (OSP Ver1 방식)
     *
     * @param githubId 사용자 GitHub ID
     * @param studentId 학번
     * @param year 계산할 연도
     * @return 계산된 점수 엔티티
     */
    @Transactional
    public GithubScoreEntity calculateScore(Long githubId, String studentId, Integer year) {
        log.info("점수 계산 시작 (Ver1): githubId={}, studentId={}, year={}", githubId, studentId, year);

        // 1. 해당 사용자의 연도별 레포별 통계 조회 (레포 이름 포함)
        List<Object[]> repoDataList = statsRepository.findAllWithRepoNameByGithubIdAndYear(githubId, year);

        // 2. 레포별 점수 계산
        List<RepoScoreData> repoScores = new ArrayList<>();
        int totalCommitCount = 0;
        int totalCommitLines = 0;
        int totalPrCount = 0;
        int totalIssueCount = 0;
        int totalStarCount = 0;
        int totalForkCount = 0;

        for (Object[] row : repoDataList) {
            GithubContributionStats stats = (GithubContributionStats) row[0];
            String repoFullName = (String) row[1];

            // OSP Ver1 공식으로 레포 점수 계산
            double commitLineScore = Math.min(stats.getCommitLines() / 10000.0, 1.0);
            double commitCntScore = Math.min(stats.getCommitCount() / 50.0, 1.0);
            double prIssueScore = Math.min((stats.getPrCount() + stats.getIssueCount()) * 0.1, 0.7);
            double guidelineScore = stats.getGuidelineScore() > 0 ? 0.3 : 0.0;
            double repoScore = commitLineScore + commitCntScore + prIssueScore + guidelineScore;

            repoScores.add(new RepoScoreData(
                    repoFullName,
                    repoScore,
                    commitLineScore,
                    commitCntScore,
                    prIssueScore,
                    guidelineScore
            ));

            // 전체 합계 (API 응답용)
            totalCommitCount += stats.getCommitCount();
            totalCommitLines += stats.getCommitLines();
            totalPrCount += stats.getPrCount();
            totalIssueCount += stats.getIssueCount();
            totalStarCount += stats.getStarCount();
            totalForkCount += stats.getForkCount();
        }

        // 3. 레포 점수 내림차순 정렬
        repoScores.sort(Comparator.comparingDouble(RepoScoreData::score).reversed());

        // 4. repo_score_sum: best repo 점수
        BigDecimal repoScoreSum = BigDecimal.ZERO;
        String bestRepo = "N/A";
        if (!repoScores.isEmpty()) {
            repoScoreSum = BigDecimal.valueOf(repoScores.get(0).score())
                    .setScale(3, RoundingMode.HALF_UP);
            bestRepo = repoScores.get(0).repoName();
        }

        // 5. score_other_repo_sum: 2nd + 3rd repo 점수 (최대 1.0)
        BigDecimal scoreOtherRepoSum = BigDecimal.ZERO;
        if (repoScores.size() > 1) {
            scoreOtherRepoSum = scoreOtherRepoSum.add(
                    BigDecimal.valueOf(repoScores.get(1).score()));
        }
        if (repoScores.size() > 2) {
            scoreOtherRepoSum = scoreOtherRepoSum.add(
                    BigDecimal.valueOf(repoScores.get(2).score()));
        }
        scoreOtherRepoSum = scoreOtherRepoSum.min(BigDecimal.ONE)
                .setScale(3, RoundingMode.HALF_UP);

        // 6. score_star: log10(star + 1)
        BigDecimal scoreStar = calculateStarScore(totalStarCount);

        // 7. score_fork: min(fork * 0.2, 1.0)
        BigDecimal scoreFork = BigDecimal.valueOf(totalForkCount)
                .multiply(BigDecimal.valueOf(0.2))
                .min(BigDecimal.ONE)
                .setScale(3, RoundingMode.HALF_UP);

        // 8. 총점 계산 (최대 5점)
        BigDecimal totalScore = repoScoreSum
                .add(scoreOtherRepoSum)
                .add(scoreStar)
                .add(scoreFork)
                .min(BigDecimal.valueOf(5.0))
                .setScale(3, RoundingMode.HALF_UP);

        log.info("점수 계산 완료: githubId={}, repoScoreSum={}, otherRepoSum={}, star={}, fork={}, total={}",
                githubId, repoScoreSum, scoreOtherRepoSum, scoreStar, scoreFork, totalScore);

        // Best repo의 세부 점수 (API 응답용)
        BigDecimal commitLineScore = BigDecimal.ZERO;
        BigDecimal commitCntScore = BigDecimal.ZERO;
        BigDecimal pullNIssueScore = BigDecimal.ZERO;
        BigDecimal guidelineScore = BigDecimal.ZERO;
        if (!repoScores.isEmpty()) {
            RepoScoreData best = repoScores.get(0);
            commitLineScore = BigDecimal.valueOf(best.commitLineScore()).setScale(3, RoundingMode.HALF_UP);
            commitCntScore = BigDecimal.valueOf(best.commitCntScore()).setScale(3, RoundingMode.HALF_UP);
            pullNIssueScore = BigDecimal.valueOf(best.prIssueScore()).setScale(3, RoundingMode.HALF_UP);
            guidelineScore = BigDecimal.valueOf(best.guidelineScore()).setScale(3, RoundingMode.HALF_UP);
        }

        // Entity 생성
        GithubScoreEntity scoreEntity = GithubScoreEntity.create(githubId, studentId, year);
        scoreEntity.updateScores(
                totalCommitCount,
                totalPrCount,
                totalIssueCount,
                totalCommitLines,
                0, // totalDeletion (commit_lines에 이미 포함)
                commitLineScore,
                commitCntScore,
                pullNIssueScore,
                guidelineScore,
                commitLineScore, // repoScoreSub
                commitCntScore,  // repoScoreAdd
                repoScoreSum,
                BigDecimal.ZERO, // scoreOtherRepoSub
                BigDecimal.ZERO, // scoreOtherRepoAdd
                scoreOtherRepoSum,
                scoreStar,
                scoreFork,
                totalScore,
                bestRepo
        );

        return scoreEntity;
    }

    /**
     * Star 점수 계산 (Ver1): log10(star + 1)
     */
    private BigDecimal calculateStarScore(int starCount) {
        if (starCount <= 0) {
            return BigDecimal.ZERO;
        }

        double logValue = Math.log10(starCount + 1);

        return BigDecimal.valueOf(logValue)
                .setScale(3, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
    }

    /**
     * 레포별 점수 데이터
     */
    private record RepoScoreData(
            String repoName,
            double score,
            double commitLineScore,
            double commitCntScore,
            double prIssueScore,
            double guidelineScore
    ) {}
}
