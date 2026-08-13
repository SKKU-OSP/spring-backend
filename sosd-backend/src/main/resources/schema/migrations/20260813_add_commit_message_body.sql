-- 기존 DB에 커밋 메시지 본문 컬럼을 추가한다.
-- 신규 DB는 schema/github-schema.sql에 이미 포함되어 있으므로 실행할 필요가 없다.
ALTER TABLE github_commit
    ADD COLUMN message_body TEXT NULL
        COMMENT '커밋 메시지 본문 (GitHub messageBody)'
        AFTER message;
