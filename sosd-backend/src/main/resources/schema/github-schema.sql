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

    -- 인덱스
    INDEX idx_github_account_login (github_login) COMMENT 'username을 통한 조회용',
    INDEX idx_github_account_last_crawling (last_crawling) COMMENT '스케쥴링할 때 기간 쿼리용',

    -- 외래키 제약조건
    CONSTRAINT fk_github_account_user
    FOREIGN KEY (student_id)
    REFERENCES user_account(student_id)
    ON DELETE CASCADE ON UPDATE CASCADE

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitHub 계정 테이블';

-- Repository 테이블
CREATE TABLE IF NOT EXISTS repository (
    repo_id BIGINT NOT NULL PRIMARY KEY COMMENT '깃허브에서 제공하는 고유 id',
    owner_name VARCHAR(255) NOT NULL COMMENT '저장소 소유자명',
    repo_name VARCHAR(255) NOT NULL COMMENT '저장소명',
    default_branch VARCHAR(255) NOT NULL COMMENT '기본 브랜치명',
    score INT DEFAULT 0 COMMENT '깃허브 점수',
    watcher INT DEFAULT 0 COMMENT '구독자 수',
    star INT DEFAULT 0 COMMENT '스타 수',
    fork INT DEFAULT 0 COMMENT '포크 수',
    commit_count INT DEFAULT 0 COMMENT '총 커밋 수',
    commit_line INT DEFAULT 0 COMMENT '총 커밋 라인 수',
    commit_del INT DEFAULT 0 COMMENT '총 삭제 라인 수',
    commit_add INT DEFAULT 0 COMMENT '총 추가 라인 수',
    unmerged_commit_count INT DEFAULT 0 COMMENT '미병합 커밋 수',
    unmerged_commit_line INT DEFAULT 0 COMMENT '미병합 커밋 라인 수',
    unmerged_commit_del INT DEFAULT 0 COMMENT '미병합 삭제 라인 수',
    unmerged_commit_add INT DEFAULT 0 COMMENT '미병합 추가 라인 수',
    pr INT DEFAULT 0 COMMENT 'Pull Request 수',
    issue INT DEFAULT 0 COMMENT 'Issue 수',
    dependency INT DEFAULT 0 COMMENT '의존성 수',
    description VARCHAR(255) COMMENT '저장소 설명',
    readme TEXT COMMENT 'README 내용',
    license VARCHAR(255) COMMENT '라이선스 이름',
    created_at DATETIME NOT NULL COMMENT '생성 일시',
    updated_at DATETIME NOT NULL COMMENT '수정 일시',
    pushed_at DATETIME NOT NULL COMMENT '마지막 푸시일시',
    language TEXT COMMENT '사용 언어 (JSON 형태)', -- 별도 테이블로 정규화할지 논의
    contributor INT DEFAULT 0 COMMENT '기여자 수',
    is_private BOOLEAN DEFAULT FALSE COMMENT '비공개 여부',
    github_id BIGINT NOT NULL COMMENT 'GitHub 계정 ID (외래키)',

    -- 인덱스
    INDEX idx_repository_github_id (github_id) COMMENT '특정 사용자의 저장소 조회용',
    INDEX idx_repository_owner_repo (owner_name, repo_name) COMMENT '저장소 full_name으로 직접 조회용 (owner/repo)',
    INDEX idx_repository_created_at (created_at) COMMENT '생성일 기준 조회 및 통계용',
    INDEX idx_repository_updated_at (updated_at) COMMENT '최근 업데이트 저장소 조회용',
    INDEX idx_repository_score (score) COMMENT '점수 기준 랭킹 및 정렬용',
    INDEX idx_repository_is_private (is_private) COMMENT '공개/비공개 저장소 필터링용',

    -- 외래키 제약조건
    CONSTRAINT fk_repository_github_account
    FOREIGN KEY (github_id)
    REFERENCES github_account(github_id)
    ON DELETE CASCADE ON UPDATE CASCADE

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitHub 저장소 테이블';