package com.sosd.sosd_backend.github_collector.collector;

import com.sosd.sosd_backend.github_collector.dto.collect.context.CollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.Cursor;

public interface GithubResourceCollector<CC extends CollectContext, RS, C extends Cursor> {
    RS collect(CC collectContext, C cursor);
    default String source() { return "generic"; }
}
