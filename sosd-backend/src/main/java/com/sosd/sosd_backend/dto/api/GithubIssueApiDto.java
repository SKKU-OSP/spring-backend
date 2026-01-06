package com.sosd.sosd_backend.dto.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Issue 데이터를 API로 전달하기 위한 DTO
 *
 * 이 클래스는 데이터베이스의 Issue 정보를
 * JSON 형태로 변환해서 API 응답으로 보내기 위해 사용됩니다.
 *
 * OSP 점수 계산에 필요한 정보:
 * - Issue 개수 카운팅 (pull_n_issue_score 계산에 사용)
 * - 점수 공식: min((pr_count + issue_count) * 0.1, 0.7)
 */
@Getter                 // 모든 필드의 getter 메서드 자동 생성
@Builder                // 빌더 패턴 사용 가능하게 함
@NoArgsConstructor      // 기본 생성자 생성
@AllArgsConstructor     // 모든 필드를 받는 생성자 생성
public class GithubIssueApiDto {

    /**
     * GitHub Issue 번호
     * 예: 456 (해당 레포의 456번 Issue)
     */
    private Integer issueNumber;

    /**
     * Issue 제목
     * 예: "기능 요청: 다크모드 추가"
     */
    private String issueTitle;

    /**
     * Issue 생성 일시
     * 예: 2024-01-15T14:30:00
     */
    private LocalDateTime issueDate;

    /**
     * Issue가 현재 열려있는지 여부
     * true: open, false: closed
     */
    private Boolean isOpen;

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
     * GitHub 사용자명 (Issue 작성자)
     * 예: "donggyu"
     */
    private String githubUsername;
}
