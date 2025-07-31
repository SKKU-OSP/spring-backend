-- src/main/resources/schema/002-github-schema.sql

-- GitHub 계정 테이블
CREATE TABLE IF NOT EXISTS github_account (
    github_id BIGINT NOT NULL PRIMARY KEY COMMENT 'GitHub ID(Github 내부적으로 사용하는 고유 id)',
    github_login VARCHAR(255) NOT NULL COMMENT 'GitHub 로그인명 (username)',
    github_name VARCHAR(255) COMMENT 'GitHub 표시명',
    github_token VARCHAR(255) COMMENT 'GitHub 액세스 토큰(private 레포까지 수집 원하는 유저만 추가)',
    github_email VARCHAR(255) NOT NULL COMMENT 'GitHub 이메일',
    last_crawling TIMESTAMP NULL COMMENT '마지막 크롤링 일시',
    student_id VARCHAR(20) NOT NULL COMMENT '연결된 학번 (외래키)',

    -- 외래키 제약조건
    CONSTRAINT fk_github_account_user
    FOREIGN KEY (student_id)
    REFERENCES user_account(student_id)
    ON DELETE CASCADE ON UPDATE CASCADE

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitHub 계정 테이블';

-- ================================
-- 인덱스 생성
-- ================================

-- username을 통한 조회용
CREATE INDEX IF NOT EXISTS idx_github_account_login ON github_account(github_login);

-- 스케쥴링할 때 기간 쿼리용
CREATE INDEX IF NOT EXISTS idx_github_account_last_crawling ON github_account(last_crawling);
