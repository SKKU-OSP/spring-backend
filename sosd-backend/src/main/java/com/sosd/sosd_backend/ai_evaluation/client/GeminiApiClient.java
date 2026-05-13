package com.sosd.sosd_backend.ai_evaluation.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.sosd_backend.ai_evaluation.dto.ReadmeEvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GeminiApiClient {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com";
    private static final Pattern JSON_BLOCK = Pattern.compile("```(?:json)?\\s*(.*?)\\s*```", Pattern.DOTALL);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-3-flash-preview}")
    private String model;

    public GeminiApiClient(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl(GEMINI_BASE_URL)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.objectMapper = objectMapper;
    }

    public ReadmeEvaluationResult evaluateReadme(String repoName, String readmeContent) {
        String prompt = buildReadmePrompt(repoName, readmeContent);
        String rawResponse = callGemini(prompt);
        return parseReadmeResult(rawResponse);
    }

    private String callGemini(String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        String url = "/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        try {
            String response = restClient.post()
                    .uri(url)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("Gemini API 호출 실패: " + e.getMessage(), e);
        }
    }

    private ReadmeEvaluationResult parseReadmeResult(String raw) {
        String json = raw.strip();
        Matcher matcher = JSON_BLOCK.matcher(json);
        if (matcher.find()) {
            json = matcher.group(1).strip();
        }
        try {
            return objectMapper.readValue(json, ReadmeEvaluationResult.class);
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", raw);
            throw new RuntimeException("Gemini 응답 파싱 실패", e);
        }
    }

    private String buildReadmePrompt(String repoName, String readmeContent) {
        return """
                당신은 학생들의 성장을 돕는 친절하고 꼼꼼한 시니어 개발자입니다.
                다음은 GitHub 리포지토리 '%s'의 README.md 내용입니다.
                제공된 평가 기준에 따라 구조와 내용을 분석하고, 핵심만 찌르되 다정하고 존중하는 어투로 피드백을 JSON 형식으로 제공하세요.

                [평가 기준]
                1. 명확성: 프로젝트의 목적, 주요 기능, 사용 기술 스택이 직관적으로 설명되어 있는가?
                2. 재현성: 처음 보는 사람도 클론받아 실행할 수 있도록 설치(Installation) 및 실행(Usage) 방법이 구체적인가?
                3. 가독성: 마크다운(Markdown) 문법을 적절히 활용하여 구조화되어 있는가?

                [제약 사항]
                1. 모든 문장의 끝맺음은 '~합니다', '~하는 것이 좋습니다', '~해 보세요' 등 정중하고 친절한 존댓말을 사용하세요.
                2. 일반적이고 추상적인 칭찬은 제외하고, README 데이터에 존재하는 팩트 기반으로만 작성하세요.
                3. strengths, improvements, advice 배열의 항목은 각각 최대 3개로 제한합니다.
                4. 각 항목은 1~2문장 이내로 핵심만 간결하게 작성하세요.
                5. 마크다운 코드 블록(```json)을 포함하지 말고, 순수 JSON 객체만 출력하세요.

                [출력 JSON 형식]
                {
                    "score": "A0",
                    "missing_essentials": ["누락된 핵심 항목. 모두 있다면 빈 배열"],
                    "strengths": ["잘한 점 1", "잘한 점 2"],
                    "improvements": ["보완할 점 1", "보완할 점 2"],
                    "advice": ["조언 1"]
                }

                [README.md 내용]
                %s
                """.formatted(repoName, readmeContent);
    }
}
