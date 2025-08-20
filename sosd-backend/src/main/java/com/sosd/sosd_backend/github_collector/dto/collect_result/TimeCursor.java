package com.sosd.sosd_backend.github_collector.dto.collect_result;

import java.time.OffsetDateTime;

public record TimeCursor(OffsetDateTime lastCollectedTime) implements Cursor{
}
