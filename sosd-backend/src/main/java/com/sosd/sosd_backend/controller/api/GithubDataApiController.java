package com.sosd.sosd_backend.controller.api;

import com.sosd.sosd_backend.dto.api.ApiResponse;
import com.sosd.sosd_backend.dto.api.GithubCommitApiDto;
import com.sosd.sosd_backend.dto.api.GithubPullRequestApiDto;
import com.sosd.sosd_backend.dto.api.GithubIssueApiDto;
import com.sosd.sosd_backend.dto.api.GithubRepositoryApiDto;
import com.sosd.sosd_backend.dto.api.GithubScoreApiDto;
import com.sosd.sosd_backend.entity.github.GithubScoreEntity;
import com.sosd.sosd_backend.service.ScoreCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * GitHub 데이터를 제공하는 REST API Controller
 *
 * 이 클래스는 외부에서 HTTP 요청이 들어오면
 * 데이터를 조회해서 JSON으로 응답합니다.
 */
@Slf4j                          // 로그를 사용할 수 있게 함 (log.info() 등)
@RestController                 // 이 클래스가 REST API Controller임을 Spring에게 알림
@RequestMapping("/api/v1/github") // 기본 URL 경로 설정
@RequiredArgsConstructor        // 생성자 자동 생성
public class GithubDataApiController {

    private final ScoreCalculationService scoreCalculationService;

    /**
     * 커밋 데이터 조회 API
     *
     * URL: GET http://localhost:8080/api/v1/github/commits
     *
     * @return 커밋 데이터 목록
     */
    @GetMapping("/commits")  // GET 요청을 받는 메서드
    public ResponseEntity<ApiResponse<GithubCommitApiDto>> getCommits() {

        // 로그 출력 (개발할 때 확인용)
        log.info("커밋 조회 API 호출됨");

        // 1. 테스트용 더미 데이터 생성
        List<GithubCommitApiDto> commits = new ArrayList<>();

        // 더미 커밋 1
        GithubCommitApiDto commit1 = GithubCommitApiDto.builder()
                .sha("abc123def456")
                .message("버그 수정")
                .addition(50)
                .deletion(20)
                .repoName("test-repo")
                .ownerName("donggyu")
                .githubUsername("donggyu")
                .build();

        // 더미 커밋 2
        GithubCommitApiDto commit2 = GithubCommitApiDto.builder()
                .sha("xyz789qwe012")
                .message("새 기능 추가")
                .addition(120)
                .deletion(5)
                .repoName("test-repo")
                .ownerName("donggyu")
                .githubUsername("donggyu")
                .build();

        // 리스트에 추가
        commits.add(commit1);
        commits.add(commit2);

        // 2. API 응답 생성
        ApiResponse<GithubCommitApiDto> response = ApiResponse.<GithubCommitApiDto>builder()
                .success(true)                      // 성공
                .message("커밋 조회 성공")            // 메시지
                .data(commits)                      // 실제 데이터
                .pagination(                        // 페이징 정보
                        ApiResponse.PaginationInfo.builder()
                                .currentPage(0)
                                .pageSize(2)
                                .totalElements(2L)
                                .totalPages(1)
                                .build()
                )
                .build();

        // 3. HTTP 200 OK와 함께 응답 반환
        return ResponseEntity.ok(response);
    }

    /**
     * Pull Request 데이터 조회 API
     *
     * URL: GET http://localhost:8080/api/v1/github/pull-requests
     *
     * @return PR 데이터 목록
     */
    @GetMapping("/pull-requests")
    public ResponseEntity<ApiResponse<GithubPullRequestApiDto>> getPullRequests() {

        // 로그 출력
        log.info("PR 조회 API 호출됨");

        // 1. 테스트용 더미 데이터 생성
        List<GithubPullRequestApiDto> prs = new ArrayList<>();

        // 더미 PR 1
        GithubPullRequestApiDto pr1 = GithubPullRequestApiDto.builder()
                .prNumber(1)
                .prTitle("기능 추가: 로그인 기능")
                .prDate(java.time.LocalDateTime.now().minusDays(5))
                .isOpen(false)
                .repoName("test-repo")
                .ownerName("donggyu")
                .githubUsername("donggyu")
                .build();

        // 더미 PR 2
        GithubPullRequestApiDto pr2 = GithubPullRequestApiDto.builder()
                .prNumber(2)
                .prTitle("버그 수정: 데이터베이스 연결 오류")
                .prDate(java.time.LocalDateTime.now().minusDays(2))
                .isOpen(true)
                .repoName("test-repo")
                .ownerName("donggyu")
                .githubUsername("donggyu")
                .build();

        prs.add(pr1);
        prs.add(pr2);

        // 2. API 응답 생성
        ApiResponse<GithubPullRequestApiDto> response = ApiResponse.<GithubPullRequestApiDto>builder()
                .success(true)
                .message("PR 조회 성공")
                .data(prs)
                .pagination(
                        ApiResponse.PaginationInfo.builder()
                                .currentPage(0)
                                .pageSize(2)
                                .totalElements(2L)
                                .totalPages(1)
                                .build()
                )
                .build();

        // 3. HTTP 200 OK와 함께 응답 반환
        return ResponseEntity.ok(response);
    }

    /**
     * Issue 데이터 조회 API
     *
     * URL: GET http://localhost:8080/api/v1/github/issues
     *
     * @return Issue 데이터 목록
     */
    @GetMapping("/issues")
    public ResponseEntity<ApiResponse<GithubIssueApiDto>> getIssues() {

        // 로그 출력
        log.info("Issue 조회 API 호출됨");

        // 1. 테스트용 더미 데이터 생성
        List<GithubIssueApiDto> issues = new ArrayList<>();

        // 더미 Issue 1
        GithubIssueApiDto issue1 = GithubIssueApiDto.builder()
                .issueNumber(10)
                .issueTitle("기능 요청: 다크모드 지원")
                .issueDate(java.time.LocalDateTime.now().minusDays(7))
                .isOpen(true)
                .repoName("test-repo")
                .ownerName("donggyu")
                .githubUsername("donggyu")
                .build();

        // 더미 Issue 2
        GithubIssueApiDto issue2 = GithubIssueApiDto.builder()
                .issueNumber(11)
                .issueTitle("버그: 회원가입 오류")
                .issueDate(java.time.LocalDateTime.now().minusDays(3))
                .isOpen(false)
                .repoName("test-repo")
                .ownerName("donggyu")
                .githubUsername("donggyu")
                .build();

        issues.add(issue1);
        issues.add(issue2);

        // 2. API 응답 생성
        ApiResponse<GithubIssueApiDto> response = ApiResponse.<GithubIssueApiDto>builder()
                .success(true)
                .message("Issue 조회 성공")
                .data(issues)
                .pagination(
                        ApiResponse.PaginationInfo.builder()
                                .currentPage(0)
                                .pageSize(2)
                                .totalElements(2L)
                                .totalPages(1)
                                .build()
                )
                .build();

        // 3. HTTP 200 OK와 함께 응답 반환
        return ResponseEntity.ok(response);
    }

    /**
     * Repository 데이터 조회 API
     *
     * URL: GET http://localhost:8080/api/v1/github/repositories
     *
     * @return Repository 데이터 목록
     */
    @GetMapping("/repositories")
    public ResponseEntity<ApiResponse<GithubRepositoryApiDto>> getRepositories() {

        // 로그 출력
        log.info("Repository 조회 API 호출됨");

        // 1. 테스트용 더미 데이터 생성
        List<GithubRepositoryApiDto> repos = new ArrayList<>();

        // 더미 Repository 1
        GithubRepositoryApiDto repo1 = GithubRepositoryApiDto.builder()
                .repoName("test-repo")
                .ownerName("donggyu")
                .fullName("donggyu/test-repo")
                .star(150)
                .fork(25)
                .contributor(10)
                .readme("# Test Repository\n\nThis is a test repository.")
                .license("MIT License")
                .description("성균관대학교 OSP 플랫폼")
                .watcher(20)
                .openIssue(5)
                .openPr(3)
                .defaultBranch("main")
                .isPrivate(false)
                .githubUsername("donggyu")
                .build();

        // 더미 Repository 2
        GithubRepositoryApiDto repo2 = GithubRepositoryApiDto.builder()
                .repoName("spring-backend")
                .ownerName("donggyu")
                .fullName("donggyu/spring-backend")
                .star(50)
                .fork(10)
                .contributor(5)
                .readme("# Spring Backend\n\nBackend service for OSP.")
                .license("Apache License 2.0")
                .description("Spring Boot 기반 백엔드 서비스")
                .watcher(8)
                .openIssue(2)
                .openPr(1)
                .defaultBranch("main")
                .isPrivate(false)
                .githubUsername("donggyu")
                .build();

        repos.add(repo1);
        repos.add(repo2);

        // 2. API 응답 생성
        ApiResponse<GithubRepositoryApiDto> response = ApiResponse.<GithubRepositoryApiDto>builder()
                .success(true)
                .message("Repository 조회 성공")
                .data(repos)
                .pagination(
                        ApiResponse.PaginationInfo.builder()
                                .currentPage(0)
                                .pageSize(2)
                                .totalElements(2L)
                                .totalPages(1)
                                .build()
                )
                .build();

        // 3. HTTP 200 OK와 함께 응답 반환
        return ResponseEntity.ok(response);
    }

    /**
     * GitHub 점수 조회 API
     *
     * URL: GET http://localhost:8080/api/v1/github/scores?githubId=123&studentId=2021&year=2024
     *
     * Spring에서 계산한 점수 결과를 반환합니다.
     * - 수집기 실행 후 점수 자동 계산
     * - OSP는 이 API로 계산된 점수만 가져가면 됨
     *
     * @param githubId GitHub ID
     * @param studentId 학번
     * @param year 연도
     * @return 계산된 점수 데이터
     */
    @GetMapping("/scores")
    public ResponseEntity<ApiResponse<GithubScoreApiDto>> getScores(
            @RequestParam(required = false) Long githubId,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Integer year
    ) {
        log.info("점수 조회 API 호출됨: githubId={}, studentId={}, year={}", githubId, studentId, year);

        // 테스트용: 점수 계산 실행
        // 실제로는 수집기가 돌고 나서 자동으로 계산되어야 함
        GithubScoreEntity scoreEntity = scoreCalculationService.calculateScore(
                githubId != null ? githubId : 123L,
                studentId != null ? studentId : "2021123456",
                year != null ? year : 2024
        );

        // Entity → DTO 변환
        GithubScoreApiDto scoreDto = GithubScoreApiDto.builder()
                .githubId(scoreEntity.getGithubId())
                .studentId(scoreEntity.getStudentId())
                .year(scoreEntity.getYear())
                .commitCount(scoreEntity.getCommitCount())
                .prCount(scoreEntity.getPrCount())
                .issueCount(scoreEntity.getIssueCount())
                .totalAddition(scoreEntity.getTotalAddition())
                .totalDeletion(scoreEntity.getTotalDeletion())
                .commitLineScore(scoreEntity.getCommitLineScore())
                .commitCntScore(scoreEntity.getCommitCntScore())
                .pullNIssueScore(scoreEntity.getPullNIssueScore())
                .guidelineScore(scoreEntity.getGuidelineScore())
                .repoScoreSum(scoreEntity.getRepoScoreSum())
                .scoreOtherRepoSum(scoreEntity.getScoreOtherRepoSum())
                .scoreStar(scoreEntity.getScoreStar())
                .scoreFork(scoreEntity.getScoreFork())
                .totalScore(scoreEntity.getTotalScore())
                .bestRepo(scoreEntity.getBestRepo())
                .build();

        // 리스트로 감싸기
        List<GithubScoreApiDto> scores = new ArrayList<>();
        scores.add(scoreDto);

        // API 응답 생성
        ApiResponse<GithubScoreApiDto> response = ApiResponse.<GithubScoreApiDto>builder()
                .success(true)
                .message("점수 조회 성공")
                .data(scores)
                .pagination(
                        ApiResponse.PaginationInfo.builder()
                                .currentPage(0)
                                .pageSize(1)
                                .totalElements(1L)
                                .totalPages(1)
                                .build()
                )
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 테스트용 헬스체크 API
     *
     * URL: GET http://localhost:8080/api/v1/github/health
     *
     * @return 간단한 상태 메시지
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("GitHub Data API is running!");
    }
}
