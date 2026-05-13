-- AI 정성적 평가 결과 저장 테이블
CREATE TABLE IF NOT EXISTS github_repo_ai_evaluation (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    github_id VARCHAR(40) NOT NULL  COMMENT 'GitHub 로그인 username',
    repo_name VARCHAR(100) NOT NULL COMMENT '레포지토리명',

    -- README 평가
    readme_score VARCHAR(10)                    COMMENT 'A+~F 등급',
    readme_missing_essentials JSON              COMMENT '누락된 필수 항목 배열',
    readme_strengths JSON                       COMMENT '잘한 점 배열 (최대 3개)',
    readme_improvements JSON                    COMMENT '보완할 점 배열 (최대 3개)',
    readme_advice JSON                          COMMENT '조언 배열',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE INDEX uq_ai_eval_github_repo (github_id, repo_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='레포지토리 AI 정성적 평가 결과';
