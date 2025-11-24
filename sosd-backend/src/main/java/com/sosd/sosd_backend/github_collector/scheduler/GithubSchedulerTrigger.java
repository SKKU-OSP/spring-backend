package com.sosd.sosd_backend.github_collector.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scheduling.github", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GithubSchedulerTrigger {

    private final GithubSimpleScheduler scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);

    // 매일 새벽 2시(Asia/Seoul) 실행
//    @Scheduled(cron = "${scheduling.github.cron:0 0 2 * * *}", zone = "Asia/Seoul")
//    public void runNightly() {
//        if (!running.compareAndSet(false, true)) {
//            // 이미 실행 중이면 스킵
//            return;
//        }
//        try {
//            scheduler.run();
//        } finally {
//            running.set(false);
//        }
//    }

    // 앱 시작 10초 뒤부터 15분 간격 실행 (테스트용으로 사용하세요)
    @Scheduled(initialDelayString = "10s", fixedDelayString = "6h")
    public void runPeriodically() {
        if (!running.compareAndSet(false, true)) {
            // 이미 실행 중이면 스킵
            return;
        }
        try {
            scheduler.run();
        } finally {
            running.set(false);
        }
    }
}
