package com.sosd.sosd_backend.dto.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 커밋 데이터를 API로 전달하기 위한 DTO
 *
 * 이 클래스는 데이터베이스의 커밋 정보를
 * JSON 형태로 변환해서 API 응답으로 보내기 위해 사용됩니다.
 */
@Getter                 // 모든 필드의 getter 메서드 자동 생성
@Builder                // 빌더 패턴 사용 가능하게 함
@NoArgsConstructor      // 기본 생성자 생성
@AllArgsConstructor     // 모든 필드를 받는 생성자 생성
public class GithubCommitApiDto {

    /**
     * 커밋의 고유 식별자 (SHA)
     * 예: "a1b2c3d4e5f6..."
     */
    private String sha;

    /**
     * 추가된 코드 라인 수
     * 예: 50 (50줄 추가)
     */
    private Integer addition;

    /**
     * 삭제된 코드 라인 수
     * 예: 20 (20줄 삭제)
     */
    private Integer deletion;

    /**
     * 커밋 작성 시간
     * 예: 2024-01-15T10:30:00
     */
    private LocalDateTime authorDate;

    /**
     * 커밋 메시지
     * 예: "버그 수정"
     */
    private String message;

    /**
     * 레포지토리 이름
     * 예: "my-project"
     */
    private String repoName;

    /**
     * 레포지토리 소유자 이름
     * 예: "donggyu"
     */
    private String ownerName;

    /**
     * GitHub 사용자명
     * 예: "donggyu"
     */
    private String githubUsername;
}
