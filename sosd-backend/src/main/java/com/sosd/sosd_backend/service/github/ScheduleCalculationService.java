package com.sosd.sosd_backend.service.github;

import com.sosd.sosd_backend.dto.github.ScheduleResult;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ScheduleCalculationService {

    // 새로운 레포 수집 관련 가중치
    private static final int BASE_ACCOUNT_SCAN_HOUR = 24;
    private static final int MAX_ACCOUNT_WEIGHT = 4;

    // 기여내역 수집 관련 가중치
    private static final int BASE_REPO_SCAN_MINUTE = 1440; // 1 day
    private static final int MAX_REPO_WEIGHT = 48;
    private static final int REPO_FOUND_BOOST = 6; // 새로운 속성 발견 시 가중치 증가 폭

    private final Random random = new Random();

    /**
     * Github Account의 다음 레포 수집 시간 계산
     * 지수 백오프 전략
     * @param currentWeight
     * @param foundNewRepos
     * @return
     */
    public ScheduleResult calculateNextAccountScan(int currentWeight, boolean foundNewRepos) {
        int newWeight = currentWeight;

        // 1. 가중치 조정
        if (foundNewRepos) {
            newWeight = Math.min(MAX_ACCOUNT_WEIGHT, currentWeight + 1); // 새로운 레포 발견 시 가중치 증가
        } else {
            newWeight = Math.max(1, currentWeight - 1); // 발견 못하면 가중치 감소
        }

        // 2. 날짜 계산
        long intervalMinutes = (long) BASE_ACCOUNT_SCAN_HOUR * 60 / newWeight;
        long jitter = random.nextInt(60); // 0~60분 사이의 무작위 지터 추가
        long totalMinutes = intervalMinutes + jitter;

        // 3. 결과 반환
        return new ScheduleResult(
                newWeight,
                java.time.LocalDateTime.now().plusMinutes(totalMinutes)
        );
    }

    public ScheduleResult calculateNextRepoScan(int currentWeight, boolean foundNewAttribute) {
        int newWeight = currentWeight;

        // 1. 가중치 조정
        if (foundNewAttribute) {
            newWeight = Math.min(MAX_REPO_WEIGHT, currentWeight + REPO_FOUND_BOOST); // 새로운 기여내역 발견 시 가중치 증가
        } else {
            newWeight = Math.max(1, currentWeight - 1); // 발견 못하면 가중치 감소
        }

        // 2. 날짜 계산
        long intervalMinutes = (long) BASE_REPO_SCAN_MINUTE / newWeight;
        long jitterRange = (long) (intervalMinutes * 0.1);
        long jitter = random.nextLong(jitterRange * 2 + 1) - jitterRange; // ±10% 사이의 무작위 jitter 추가
        long totalMinutes = intervalMinutes + jitter;

        // 3. 결과 반환
        return new ScheduleResult(
                newWeight,
                java.time.LocalDateTime.now().plusMinutes(totalMinutes)
        );
    }

}
