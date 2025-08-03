-- src/main/resources/schema/user-schema.sql

-- 유저 테이블
CREATE TABLE IF NOT EXISTS user_account (
    student_id VARCHAR(20) NOT NULL PRIMARY KEY COMMENT '학번 (SSO)',
    name VARCHAR(40) NOT NULL COMMENT '이름(SSO)',
    role INT NOT NULL DEFAULT 0 COMMENT '역할 (0: 일반회원, 1: 관리자 등), 추후 역할 추가 가능성이 있기에 int로 설정',
    college VARCHAR(255) NOT NULL COMMENT '단과대학',
    dept VARCHAR(255) NOT NULL COMMENT '학과',
    plural_major INT DEFAULT 0 COMMENT '복수전공 여부',
    photo VARCHAR(255) COMMENT '프로필 사진 URL',
    introduction TEXT COMMENT '자기소개',
    portfolio TEXT COMMENT '포트폴리오',
    date_joined TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',
    last_login TIMESTAMP NULL COMMENT '마지막 로그인',
    absence INT NOT NULL DEFAULT 0 COMMENT '재학 여부 (0: 재학, 1: 휴학, 2: 졸업 등)',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 상태'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 계정 테이블';

-- ================================
-- 인덱스 생성
-- ================================

-- 재학 여부 필터용
CREATE INDEX IF NOT EXISTS idx_user_absence ON user_account(absence);

-- 재학생 통계용
CREATE INDEX IF NOT EXISTS idx_user_active_college_dept ON user_account(is_active, college, dept, absence);

-- 가입자 분석용
CREATE INDEX IF NOT EXISTS idx_user_date_joined ON user_account(date_joined);

-- 활동 분석용
CREATE INDEX IF NOT EXISTS idx_user_last_login ON user_account(last_login);