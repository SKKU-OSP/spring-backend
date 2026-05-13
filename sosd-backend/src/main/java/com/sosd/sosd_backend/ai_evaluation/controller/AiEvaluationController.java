package com.sosd.sosd_backend.ai_evaluation.controller;

import com.sosd.sosd_backend.ai_evaluation.dto.AiEvaluationResponse;
import com.sosd.sosd_backend.ai_evaluation.service.ReadmeEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v2/ai-evaluation")
@RequiredArgsConstructor
public class AiEvaluationController {

    private final ReadmeEvaluationService readmeEvaluationService;

    /**
     * 저장된 README 평가 결과 조회
     * GET /api/v2/ai-evaluation/readme?githubUsername=foo&repoName=bar
     */
    @GetMapping("/readme")
    public ResponseEntity<?> getReadmeEvaluation(
            @RequestParam String githubUsername,
            @RequestParam String repoName
    ) {
        Optional<AiEvaluationResponse> result =
                readmeEvaluationService.getEvaluation(githubUsername, repoName);

        if (result.isPresent()) {
            return ResponseEntity.ok(Map.of("status", "success", "data", result.get()));
        }
        return ResponseEntity.ok(Map.of("status", "success", "data", (Object) null));
    }

    /**
     * README 평가 실행 (DB의 README로 Gemini 호출 후 저장)
     * POST /api/v2/ai-evaluation/readme
     * body: { "githubUsername": "foo", "repoName": "bar" }
     */
    @PostMapping("/readme")
    public ResponseEntity<?> evaluateReadme(@RequestBody Map<String, String> body) {
        String githubUsername = body.get("githubUsername");
        String repoName = body.get("repoName");

        if (githubUsername == null || repoName == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "fail", "message", "githubUsername, repoName은 필수입니다."));
        }

        try {
            AiEvaluationResponse result = readmeEvaluationService.evaluate(githubUsername, repoName);
            return ResponseEntity.ok(Map.of("status", "success", "data", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "fail", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("README 평가 실패: {}/{} - {}", githubUsername, repoName, e.getMessage());
            String msg = e.getMessage() != null && e.getMessage().contains("429")
                    ? "AI 서비스 호출 쿼터가 초과되었습니다. 잠시 후 다시 시도해 주세요."
                    : "AI 분석 중 오류가 발생했습니다.";
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "fail", "message", msg));
        }
    }
}
