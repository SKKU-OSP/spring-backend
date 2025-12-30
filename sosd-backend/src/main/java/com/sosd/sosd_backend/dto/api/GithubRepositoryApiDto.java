package com.sosd.sosd_backend.dto.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Repository 데이터를 API로 전달하기 위한 DTO
 *
 * 이 클래스는 데이터베이스의 저장소 정보를
 * JSON 형태로 변환해서 API 응답으로 보내기 위해 사용됩니다.
 *
 * OSP 점수 계산에 필요한 정보:
 * 1. Star 점수: min(log10((star + 1.1) / 3), 2) - 최대 2점
 * 2. Fork 점수: min(fork * 0.1, 1.0) - 최대 1점
 * 3. Guideline 점수: README + License + Description 있으면 0.3점
 * 4. 기여자 수: Contributor 카운트 (통계용)
 */
@Getter                 // 모든 필드의 getter 메서드 자동 생성
@Builder                // 빌더 패턴 사용 가능하게 함
@NoArgsConstructor      // 기본 생성자 생성
@AllArgsConstructor     // 모든 필드를 받는 생성자 생성
public class GithubRepositoryApiDto {

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
     * 레포지토리 전체 이름 (owner/repo)
     * 예: "donggyu/my-project"
     */
    private String fullName;

    /**
     * 스타 개수
     * 예: 150
     * 점수 계산: min(log10((150 + 1.1) / 3), 2)
     */
    private Integer star;

    /**
     * 포크 개수
     * 예: 25
     * 점수 계산: min(25 * 0.1, 1.0) = 1.0점
     */
    private Integer fork;

    /**
     * 기여자 수
     * 예: 10 (10명이 기여)
     */
    private Integer contributor;

    /**
     * README 내용
     * 있으면 guideline 점수 계산에 포함 (0.1점)
     */
    private String readme;

    /**
     * 라이선스 이름
     * 예: "MIT License"
     * 있으면 guideline 점수 계산에 포함 (0.1점)
     */
    private String license;

    /**
     * 저장소 설명
     * 예: "성균관대학교 OSP 플랫폼"
     * 있으면 guideline 점수 계산에 포함 (0.1점)
     */
    private String description;

    /**
     * 구독자 수 (Watcher)
     * 예: 20
     */
    private Integer watcher;

    /**
     * Open Issue 개수
     * 예: 5
     */
    private Integer openIssue;

    /**
     * Open PR 개수
     * 예: 3
     */
    private Integer openPr;

    /**
     * 기본 브랜치명
     * 예: "main" or "master"
     */
    private String defaultBranch;

    /**
     * 비공개 여부
     * true: private, false: public
     */
    private Boolean isPrivate;

    /**
     * GitHub 사용자명 (레포 소유자 또는 기여자)
     * 예: "donggyu"
     */
    private String githubUsername;
}
