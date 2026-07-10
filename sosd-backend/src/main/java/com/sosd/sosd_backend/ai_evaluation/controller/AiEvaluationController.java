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
            String m = e.getMessage() != null ? e.getMessage() : "";
            String msg;
            if (m.contains("429") || m.contains("RESOURCE_EXHAUSTED")) {
                msg = "AI 사용량 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.";
            } else if (m.contains("503") || m.contains("UNAVAILABLE") || m.contains("high demand")) {
                msg = "AI 서버가 일시적으로 혼잡합니다. 잠시 후 다시 시도해 주세요.";
            } else if (m.contains("timed out") || m.contains("Read timed out")) {
                msg = "AI 응답 시간이 초과되었습니다. README가 너무 길거나 서버가 혼잡할 수 있습니다.";
            } else {
                msg = "AI 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
            }
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "fail", "message", msg));
        }
    }
}
