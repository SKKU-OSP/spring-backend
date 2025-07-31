-- src/main/resources/schema/user-schema.sql

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
    absence INT NOT NULL DEFAULT 0 COMMENT '휴학',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 상태'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 계정 테이블';
