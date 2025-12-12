package com.sosd.sosd_backend.dto.github;

import java.time.LocalDateTime;

public record ScheduleResult(
        int weight,
        LocalDateTime nextTime
) {}
