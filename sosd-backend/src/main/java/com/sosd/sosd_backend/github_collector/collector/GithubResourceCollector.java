package com.sosd.sosd_backend.github_collector.collector;

import com.sosd.sosd_backend.github_collector.dto.collect.context.CollectContext;
import com.sosd.sosd_backend.github_collector.dto.collect.result.CollectResult;
import com.sosd.sosd_backend.github_collector.dto.collect.result.Cursor;
import org.springframework.stereotype.Component;

public interface GithubResourceCollector<CC extends CollectContext, T, C extends Cursor> {
    CollectResult<T,C> collect(CC collectContext);
    default String source() { return "generic"; }
}
