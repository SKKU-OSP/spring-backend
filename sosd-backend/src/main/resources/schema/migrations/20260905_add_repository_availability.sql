ALTER TABLE github_repository
    ADD COLUMN availability_status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE'
        COMMENT '공개 접근 가능 상태' AFTER last_collected_at,
    ADD COLUMN consecutive_unavailable_count INT NOT NULL DEFAULT 0
        COMMENT '연속 NOT_FOUND 확인 횟수' AFTER availability_status,
    ADD COLUMN last_availability_checked_at DATETIME NULL
        COMMENT '마지막 공개 접근 확인 시각' AFTER consecutive_unavailable_count,
    ADD COLUMN unavailable_since DATETIME NULL
        COMMENT '연속 접근 불가가 시작된 시각' AFTER last_availability_checked_at,
    ADD COLUMN last_availability_error VARCHAR(512) NULL
        COMMENT '최근 접근 불가 확인 메시지' AFTER unavailable_since,
    ADD INDEX idx_repository_availability (availability_status, is_private);
