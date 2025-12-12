package com.sosd.sosd_backend.constant;

public enum CollectionStatus {
    // Happy Path
    READY,      // 대기 중 (스케줄러 조회 대상)
    QUEUED,     // 큐에 등록됨/작업 중 (Lock 역할)

    // Repair Path
    NAME_RESCUE, // 404 발생 -> 이름/경로 확인 필요
    DIVERGED,    // 정합성 깨짐 -> 전수 조사 필요

    // Terminal Path
    DELETED,     // 삭제됨
    BLOCKED      // 차단됨
}
