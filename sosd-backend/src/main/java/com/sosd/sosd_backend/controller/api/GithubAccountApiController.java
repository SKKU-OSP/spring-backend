package com.sosd.sosd_backend.controller.api;

import com.sosd.sosd_backend.service.github.GithubAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * GitHub 계정 관리 API Controller
 *
 * OSP에서 github_id(username) 변경 시 Spring DB와 동기화하기 위한 엔드포인트를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/github-account")
@RequiredArgsConstructor
public class GithubAccountApiController {

    private final GithubAccountService githubAccountService;

    /**
     * GitHub 로그인 username 변경 API
     *
     * URL: PATCH /api/v1/github-account/{studentId}/username
     *
     * OSP에서 사용자의 github_id(username)가 변경될 때 호출합니다.
     * Spring의 github_account.github_login_username을 업데이트하면
     * 해당 컬럼을 참조하는 모든 VIEW(v_github_repo_commits 등)가 자동으로 반영됩니다.
     *
     * @param studentId 학번
     * @param body      { "newUsername": "new_github_login" }
     */
    @PatchMapping("/{studentId}/username")
    public ResponseEntity<Map<String, Object>> updateUsername(
            @PathVariable String studentId,
            @RequestBody Map<String, String> body
    ) {
        String newUsername = body.get("newUsername");
        if (newUsername == null || newUsername.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "newUsername은 필수입니다."));
        }

        log.info("GitHub username 변경 요청: studentId={}, newUsername={}", studentId, newUsername);

        boolean updated = githubAccountService.updateGithubLoginUsername(studentId, newUsername);
        if (!updated) {
            log.warn("GitHub username 변경 실패: studentId={}에 해당하는 계정 없음", studentId);
            return ResponseEntity.ok(Map.of("success", false, "message", "해당 학번의 GitHub 계정을 찾을 수 없습니다."));
        }

        log.info("GitHub username 변경 완료: studentId={}, newUsername={}", studentId, newUsername);
        return ResponseEntity.ok(Map.of("success", true, "message", "GitHub username이 변경되었습니다."));
    }
}
