-- src/main/resources/schema/002-github-schema.sql

-- GitHub 계정 테이블
CREATE TABLE IF NOT EXISTS github_account (
    github_id BIGINT NOT NULL PRIMARY KEY COMMENT 'GitHub ID(Github 내부적으로 사용하는 고유 id)',
    github_login_username VARCHAR(255) NOT NULL COMMENT 'GitHub 로그인명 (username)',
    github_name VARCHAR(255) COMMENT 'GitHub 표시명',
    github_token VARCHAR(255) COMMENT 'GitHub 액세스 토큰(private 레포까지 수집 원하는 유저만 추가)',
    github_email VARCHAR(255) NOT NULL COMMENT 'GitHub 이메일',
    last_crawling TIMESTAMP NULL COMMENT '마지막 크롤링 일시',
    student_id VARCHAR(20) NOT NULL COMMENT '연결된 학번 (외래키)',

    -- 인덱스
    INDEX idx_github_account_login (github_login_username) COMMENT 'username을 통한 조회용',
    INDEX idx_github_account_last_crawling (last_crawling) COMMENT '스케쥴링할 때 기간 쿼리용',

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
    github_repository_created_at DATETIME NOT NULL COMMENT '생성 일시',
    github_repository_updated_at DATETIME NOT NULL COMMENT '수정 일시',
    pushed_at DATETIME NOT NULL COMMENT '마지막 푸시일시',
    additional_data TEXT COMMENT '사용 언어 등 추가 정보 (JSON 형태)', -- 별도 테이블로 정규화할지 논의
    contributor INT DEFAULT 0 COMMENT '기여자 수',
    is_private BOOLEAN DEFAULT FALSE COMMENT '비공개 여부',
    github_id BIGINT NOT NULL COMMENT 'GitHub 계정 ID (외래키)',

    -- 인덱스
    INDEX idx_repository_github_id (github_id, is_private) COMMENT '특정 사용자의 저장소 조회용',
    INDEX idx_repository_owner_repo (owner_name, repo_name, is_private) COMMENT '저장소 full_name으로 직접 조회용 (owner/repo)',
    INDEX idx_repository_created_at (github_repository_created_at,is_private) COMMENT '생성일 기준 조회 및 통계용',
    INDEX idx_repository_updated_at (github_repository_updated_at,is_private) COMMENT '최근 업데이트 저장소 조회용',
    INDEX idx_repository_score (score, is_private) COMMENT '점수 기준 랭킹 및 정렬용',

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitHub 레포지토리 테이블';

-- Commit 테이블
CREATE TABLE IF NOT EXISTS commit (
    sha VARCHAR(40) NOT NULL PRIMARY KEY COMMENT 'Git commit SHA (40자 고정길이 해시값)',
    addition INT NOT NULL COMMENT '추가된 라인 수',
    deletion INT NOT NULL COMMENT '삭제된 라인 수',
    author_date DATETIME NOT NULL COMMENT '커밋 작성자 시간',
    committer_date DATETIME NOT NULL COMMENT '푸시 시간',
    message TEXT COMMENT '커밋 메시지',
    branch VARCHAR(255) NOT NULL COMMENT '브랜치명',
    repo_id BIGINT NOT NULL COMMENT '저장소 ID (외래키)',

    -- 인덱스

    INDEX idx_commit_branch (branch) COMMENT 'default 브랜치 병합 조회용',
    INDEX idx_commit_repo_id (repo_id) COMMENT '레포지토리 기준 커밋 조회용',


    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Github 커밋 테이블';

-- Pull Request 테이블
CREATE TABLE IF NOT EXISTS pull_request (
    pr_id BIGINT NOT NULL PRIMARY KEY COMMENT 'GitHub Pull Request ID (깃허브에서 제공하는 고유 id)',
    pr_number INT NOT NULL COMMENT 'Pull Request 번호',
    pr_title VARCHAR(255) NOT NULL COMMENT 'Pull Request 제목',
    pr_body TEXT COMMENT 'Pull Request 본문',
    pr_date DATETIME NOT NULL COMMENT 'Pull Request 생성일시',
    merged BOOLEAN DEFAULT FALSE COMMENT '병합 여부',
    base_branch VARCHAR(255) NOT NULL COMMENT '기준 브랜치 (병합 대상)',
    head_branch VARCHAR(255) NOT NULL COMMENT '소스 브랜치 (병합 소스)',
    repo_id BIGINT NOT NULL COMMENT '저장소 ID (외래키)',

    -- 인덱스
    INDEX idx_repo_id_pr_number (repo_id, pr_number) COMMENT 'pr 조회용',
    INDEX idx_merged (merged) COMMENT '병합 여부 조회용',
    INDEX idx_pr_date (pr_date) COMMENT 'pr 생성 날짜 필터용',

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitHub Pull Request 테이블';

-- Issue 테이블
CREATE TABLE IF NOT EXISTS issue (
    issue_id BIGINT NOT NULL PRIMARY KEY COMMENT 'GitHub Issue ID (깃허브에서 제공하는 고유 id)',
    issue_number INT NOT NULL COMMENT 'Issue 번호',
    issue_title VARCHAR(255) NOT NULL COMMENT 'Issue 제목',
    issue_body TEXT COMMENT 'Issue 본문',
    issue_date DATETIME NOT NULL COMMENT 'Issue 생성일시',
    repo_id BIGINT NOT NULL COMMENT '저장소 ID (외래키)',

    -- 인덱스
    INDEX idx_repo_id_issue_number (repo_id, issue_number) COMMENT 'issue 조회용',
    INDEX idx_issue_date (issue_date) COMMENT 'issue 생성 날짜 조회용',

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitHub Issue 테이블';

-- Fork 테이블
CREATE TABLE IF NOT EXISTS fork (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Auto increment ID',
    fork_user_id BIGINT NOT NULL COMMENT 'Fork한 사용자 ID',
    fork_date DATETIME NOT NULL COMMENT 'Fork 일시',
    repo_id BIGINT NOT NULL COMMENT '저장소 ID (외래키)',

    -- 인덱스
    INDEX idx_fork_repo_id (repo_id) COMMENT '해당 레포의 fork 조회용',
    INDEX idx_fork_date (fork_date) COMMENT 'fork 날짜 필터용',

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitHub Fork 테이블';

-- Star 테이블
CREATE TABLE IF NOT EXISTS star (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Auto increment ID',
    star_user_id BIGINT NOT NULL COMMENT 'Star한 사용자 ID',
    star_date DATETIME NOT NULL COMMENT 'Star 일시',
    repo_id BIGINT NOT NULL COMMENT '저장소 ID (외래키)',

    -- 인덱스
    INDEX idx_star_repo_id (repo_id) COMMENT '해당 레포의 star 조회용',
    INDEX idx_star_date (star_date) COMMENT 'star 날짜 필터용',

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitHub Star 테이블';