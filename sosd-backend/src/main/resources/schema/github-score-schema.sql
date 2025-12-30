-- GitHub 점수 계산 결과 저장 테이블
-- OSP의 updateScore.py 로직을 Spring에서 계산한 결과를 저장

CREATE TABLE IF NOT EXISTS github_score (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    github_id BIGINT NOT NULL COMMENT 'GitHub 계정 ID (외래키)',
    student_id VARCHAR(20) NOT NULL COMMENT '학번',
    year INT NOT NULL COMMENT '점수 계산 연도',

    -- 기본 통계 정보
    commit_count INT DEFAULT 0 COMMENT '총 커밋 개수',
    pr_count INT DEFAULT 0 COMMENT '총 PR 개수',
    issue_count INT DEFAULT 0 COMMENT '총 Issue 개수',
    total_addition INT DEFAULT 0 COMMENT '총 추가 라인 수',
    total_deletion INT DEFAULT 0 COMMENT '총 삭제 라인 수',

    -- 점수 계산 결과
    commit_line_score DECIMAL(10, 3) DEFAULT 0.0 COMMENT '코드 라인 점수 (10000줄당 1점, 최대 1점)',
    commit_cnt_score DECIMAL(10, 3) DEFAULT 0.0 COMMENT '커밋 개수 점수 (50개당 1점, 최대 1점)',
    pull_n_issue_score DECIMAL(10, 3) DEFAULT 0.0 COMMENT 'PR+Issue 점수 (각 0.1점, 최대 0.7점)',
    guideline_score DECIMAL(10, 3) DEFAULT 0.0 COMMENT 'README+License+Description 점수 (0.3점)',

    -- 레포지토리 점수
    repo_score_sub DECIMAL(10, 3) DEFAULT 0.0 COMMENT '자기 레포 삭제 라인 점수',
    repo_score_add DECIMAL(10, 3) DEFAULT 0.0 COMMENT '자기 레포 추가 라인 점수',
    repo_score_sum DECIMAL(10, 3) DEFAULT 0.0 COMMENT '자기 레포 총점 (sub + add)',

    -- 타인 레포 기여 점수
    score_other_repo_sub DECIMAL(10, 3) DEFAULT 0.0 COMMENT '타인 레포 삭제 라인 점수',
    score_other_repo_add DECIMAL(10, 3) DEFAULT 0.0 COMMENT '타인 레포 추가 라인 점수',
    score_other_repo_sum DECIMAL(10, 3) DEFAULT 0.0 COMMENT '타인 레포 총점',

    -- Star/Fork 점수
    score_star DECIMAL(10, 3) DEFAULT 0.0 COMMENT 'Star 점수 (log 계산, 최대 2점)',
    score_fork DECIMAL(10, 3) DEFAULT 0.0 COMMENT 'Fork 점수 (0.1씩, 최대 1점)',

    -- 최종 점수
    total_score DECIMAL(10, 3) DEFAULT 0.0 COMMENT '총점 (최대 5점)',

    -- 메타 정보
    best_repo VARCHAR(512) COMMENT '최고 점수 레포지토리',
    last_calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '마지막 계산 시간',

    -- 인덱스
    UNIQUE INDEX uq_github_score_year (github_id, year) COMMENT 'github_id + year 조합 유니크',
    INDEX idx_github_score_student_id (student_id) COMMENT '학번으로 조회',
    INDEX idx_github_score_year (year) COMMENT '연도별 조회',
    INDEX idx_github_score_total (total_score DESC) COMMENT '점수 순위 조회',
    INDEX idx_github_score_calculated (last_calculated_at) COMMENT '최근 계산 시간 조회'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitHub 활동 점수 계산 결과 테이블';
