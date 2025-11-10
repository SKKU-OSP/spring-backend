CREATE TABLE IF NOT EXISTS github_contribution_stats (
     id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
     github_id BIGINT NOT NULL COMMENT '유저 github_id',
     repo_id BIGINT NOT NULL COMMENT '레포 ID',
     year INT NOT NULL COMMENT '집계 연도',

     commit_count INT DEFAULT 0,
     commit_lines INT DEFAULT 0,
     pr_count INT DEFAULT 0,
     issue_count INT DEFAULT 0,
     guideline_score DOUBLE DEFAULT 0,
     repo_score DOUBLE DEFAULT 0,
     star_count INT DEFAULT 0,
     fork_count INT DEFAULT 0,

     last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

     UNIQUE INDEX uq_user_repo_year (github_id, repo_id, year)
     UNIQUE INDEX uq_user_repo_update (github_id, repo_id, last_updated_at);
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='연도별 유저-레포 집계 점수 테이블';
