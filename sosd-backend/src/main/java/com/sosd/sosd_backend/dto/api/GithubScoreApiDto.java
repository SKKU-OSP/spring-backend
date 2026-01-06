package com.sosd.sosd_backend.dto.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * GitHub 점수 데이터를 API로 전달하기 위한 DTO
 *
 * Spring에서 계산한 점수 결과를 OSP로 전달합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubScoreApiDto {

    /**
     * GitHub 계정 ID
     */
    private Long githubId;

    /**
     * 학번
     */
    private String studentId;

    /**
     * 계산 연도
     */
    private Integer year;

    // ===== 기본 통계 =====

    /**
     * 총 커밋 개수
     */
    private Integer commitCount;

    /**
     * 총 PR 개수
     */
    private Integer prCount;

    /**
     * 총 Issue 개수
     */
    private Integer issueCount;

    /**
     * 총 추가 라인 수
     */
    private Integer totalAddition;

    /**
     * 총 삭제 라인 수
     */
    private Integer totalDeletion;

    // ===== 점수 상세 =====

    /**
     * 코드 라인 점수 (최대 1점)
     */
    private BigDecimal commitLineScore;

    /**
     * 커밋 개수 점수 (최대 1점)
     */
    private BigDecimal commitCntScore;

    /**
     * PR + Issue 점수 (최대 0.7점)
     */
    private BigDecimal pullNIssueScore;

    /**
     * README + License + Description 점수 (0.3점)
     */
    private BigDecimal guidelineScore;

    /**
     * 자기 레포 점수 합계
     */
    private BigDecimal repoScoreSum;

    /**
     * 타인 레포 기여 점수 합계
     */
    private BigDecimal scoreOtherRepoSum;

    /**
     * Star 점수 (최대 2점)
     */
    private BigDecimal scoreStar;

    /**
     * Fork 점수 (최대 1점)
     */
    private BigDecimal scoreFork;

    /**
     * 총점 (최대 5점)
     */
    private BigDecimal totalScore;

    /**
     * 최고 점수 레포지토리
     */
    private String bestRepo;
}
