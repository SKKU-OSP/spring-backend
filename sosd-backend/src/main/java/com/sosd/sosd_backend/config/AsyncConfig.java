package com.sosd.sosd_backend.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "githubCollectorTaskExecutor")
    public Executor githubCollectorTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. 기본 스레드 개수 (평상시 대기하는 스레드 수)
        executor.setCorePoolSize(10);

        // 2. 최대 스레드 개수
        executor.setMaxPoolSize(30);

        // 3. 대기열 크기
        executor.setQueueCapacity(100);

        // 4. 스레드 이름 접두사
        executor.setThreadNamePrefix("Github-Col-");

        // 5. 애플리케이션 종료 시 진행 중인 작업 마무리 대기 (Graceful Shutdown)
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120); // 최대 120초 대기 -> Github 내역 풀스캔이 발생할 경우 2분까지 발생할 수도

        // 6. [중요] MDC 컨텍스트 복사 (로그 추적용)
        executor.setTaskDecorator(new MdcTaskDecorator());

        executor.initialize();
        return executor;
    }

    public static class MdcTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            // 현재 스레드(Main)의 MDC 컨텍스트 맵을 가져옴
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    // 자식 스레드(Worker)에 컨텍스트 복원
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    // 작업 끝나면 정리 (스레드 풀 재사용을 위해 필수)
                    MDC.clear();
                }
            };
        }
    }
}
