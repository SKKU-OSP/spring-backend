package com.sosd.sosd_backend.service;

import com.sosd.sosd_backend.entity.github.GithubScoreEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@ConditionalOnMissingBean(ScoreCalculator.class)
public class ScoreCalculationServiceDummy implements ScoreCalculator {

    @Override
    public GithubScoreEntity calculateScore(Long githubId, String studentId, Integer year) {
        log.info("점수 계산 (dummy): githubId={}, studentId={}, year={}", githubId, studentId, year);

        GithubScoreEntity scoreEntity = GithubScoreEntity.create(githubId, studentId, year);
        scoreEntity.updateScores(
                0, 0, 0, 0, 0,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO,
                "N/A"
        );

        return scoreEntity;
    }
}