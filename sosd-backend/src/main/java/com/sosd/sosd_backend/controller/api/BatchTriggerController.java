package com.sosd.sosd_backend.controller.api;

import com.sosd.sosd_backend.data_aggregation.launcher.StatsJobLauncher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Batch Job 수동 트리거용 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
public class BatchTriggerController {

    private final StatsJobLauncher statsJobLauncher;

    /**
     * Contribution Stats Batch Job 수동 실행
     * POST /api/v1/batch/contribution-stats/run
     */
    @PostMapping("/contribution-stats/run")
    public ResponseEntity<Map<String, String>> runContributionStatsJob() {
        log.info("Batch Job 수동 트리거 요청");

        try {
            statsJobLauncher.runContributionStatsJob();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Contribution Stats Batch Job이 시작되었습니다."
            ));
        } catch (Exception e) {
            log.error("Batch Job 실행 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}
