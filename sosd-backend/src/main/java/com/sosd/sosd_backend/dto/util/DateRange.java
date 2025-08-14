package com.sosd.sosd_backend.dto.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * 날짜 범위를 나타내는 유틸리티 DTO
 *
 * @param from 시작 날짜시간 (inclusive)
 * @param to 종료 날짜시간 (inclusive)
 */
public record DateRange(
        OffsetDateTime from,
        OffsetDateTime to
) {

    /**
     * 시작과 종료 날짜시간을 모두 지정
     */
    public static DateRange of(OffsetDateTime from, OffsetDateTime to) {
        return new DateRange(from, to);
    }

    /**
     * 시작 날짜시간부터 현재까지
     */
    public static DateRange fromDate(OffsetDateTime from) {
        return new DateRange(from, OffsetDateTime.now());
    }

    /**
     * 올해 1월 1일 00:00:00부터 현재까지 (UTC 기준)
     */
    public static DateRange thisYear() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startOfYear = OffsetDateTime.of(
                now.getYear(), 1, 1,
                0, 0, 0, 0,
                ZoneOffset.UTC
        );
        return new DateRange(startOfYear, now);
    }

}