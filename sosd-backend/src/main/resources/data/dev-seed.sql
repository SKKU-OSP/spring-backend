-- 기본 유저 계정 데이터
INSERT INTO user_account (
    student_id, name, role, college, dept, plural_major, photo, introduction, portfolio,
    date_joined, updated_at, last_login, absence, is_active
)
VALUES (
           '2020315013', -- 학번
           '강병희', -- 이름
           0,           -- 역할
           '소프트웨어대학', -- 단과대학
           '소프트웨어학과', -- 학과
           0,           -- 복수전공 여부
           NULL,        -- 프로필 사진
           '안녕하세요. 테스트 계정입니다.', -- 자기소개
           NULL,        -- 포트폴리오
           NOW(),       -- 가입일
           NOW(),       -- 업데이트일
           NULL,        -- 마지막 로그인
           0,           -- 재학
           TRUE         -- 활성 상태
       )
    ON DUPLICATE KEY UPDATE
                         name = VALUES(name),
                         updated_at = NOW();

-- GitHub 계정 데이터 (연결된 유저)
INSERT INTO github_account (
    github_id, github_graphql_node_id, github_login_username, github_name, github_token, github_email, last_crawling, student_id
)
VALUES (
           80045655,              -- GitHub ID
           'MDQ6VXNlcjgwMDQ1NjU1',
           'byungKHee',             -- GitHub username
           'Byunghee Kang',         -- GitHub 표시명
           NULL,                   -- GitHub 토큰
           'byungheekang@g.skku.edu', -- 이메일
           NULL,                   -- 마지막 크롤링
           '2020315013'            -- 연결 학번
       )
    ON DUPLICATE KEY UPDATE
                         github_name = VALUES(github_name),
                         github_email = VALUES(github_email),
                         last_crawling = VALUES(last_crawling);


-- 기본 유저 계정 데이터
INSERT INTO user_account (
    student_id, name, role, college, dept, plural_major, photo, introduction, portfolio,
    date_joined, updated_at, last_login, absence, is_active
)
VALUES (
           '2020999999', -- 학번 (더미)
           '강동윤',     -- 이름 (더미)
           0,            -- 역할 (기본 0: 학생)
           '소프트웨어대학', -- 단과대학 (더미)
           '소프트웨어학과', -- 학과 (더미)
           0,            -- 복수전공 여부
           NULL,         -- 프로필 사진
           '성능 테스트용 계정', -- 자기소개 (더미)
           NULL,         -- 포트폴리오
           NOW(),        -- 가입일
           NOW(),        -- 업데이트일
           NULL,         -- 마지막 로그인
           0,            -- 재학 여부
           TRUE          -- 활성 상태
       )
ON DUPLICATE KEY UPDATE
                     name = VALUES(name),
                     updated_at = NOW();

-- GitHub 계정 데이터 (연결된 유저)
INSERT INTO github_account (
    github_id, github_graphql_node_id, github_login_username, github_name,
    github_token, github_email, last_crawling, student_id
)
VALUES (
           29931815,              -- GitHub ID
           'MDQ6VXNlcjI5OTMxODE1', -- GraphQL Node ID
           'kdy1',                 -- GitHub username
           'KDY',                  -- GitHub 표시명 (더미)
           NULL,                   -- GitHub 토큰
           'kdy1@example.com',     -- 이메일 (더미)
           NULL,                   -- 마지막 크롤링
           '2020999999'            -- 연결 학번 (위 user_account와 동일)
       )
ON DUPLICATE KEY UPDATE
                     github_name = VALUES(github_name),
                     github_email = VALUES(github_email),
                     last_crawling = VALUES(last_crawling);

-- 기본 유저 계정 데이터
INSERT INTO user_account (
    student_id, name, role, college, dept, plural_major, photo, introduction, portfolio,
    date_joined, updated_at, last_login, absence, is_active
)
VALUES (
           '2020888888',              -- 학번 (더미)
           '김기태',                  -- 이름 (GitHub name 기반)
           0,                         -- 역할 (0: 학생)
           '소프트웨어대학',          -- 단과대학 (더미)
           '소프트웨어학과',          -- 학과 (더미)
           0,                         -- 복수전공 여부
           NULL,                      -- 프로필 사진
           '성능 테스트용 계정',       -- 자기소개 (더미)
           NULL,                      -- 포트폴리오
           NOW(),                     -- 가입일
           NOW(),                     -- 업데이트일
           NULL,                      -- 마지막 로그인
           0,                         -- 재학 여부
           TRUE                       -- 활성 상태
       )
ON DUPLICATE KEY UPDATE
                     name = VALUES(name),
                     updated_at = NOW();

-- GitHub 계정 데이터 (연결된 유저)
INSERT INTO github_account (
    github_id, github_graphql_node_id, github_login_username, github_name,
    github_token, github_email, last_crawling, student_id
)
VALUES (
           99127993,                  -- GitHub databaseId
           'U_kgDOBeiSuQ',            -- GraphQL Node ID
           'ki011127',                -- GitHub username (login)
           'KITAE KIM',               -- GitHub 표시명
           NULL,                      -- GitHub 토큰
           'ki011127@example.com',    -- 이메일 (더미)
           NULL,                      -- 마지막 크롤링
           '2020888888'               -- 연결 학번 (위 user_account와 동일)
       )
ON DUPLICATE KEY UPDATE
                     github_name   = VALUES(github_name),
                     github_email  = VALUES(github_email),
                     last_crawling = VALUES(last_crawling);
