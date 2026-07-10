package com.sosd.sosd_backend.ai_evaluation.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.sosd_backend.ai_evaluation.dto.GeminiScoreResponse;
import com.sosd.sosd_backend.ai_evaluation.dto.GeminiSentenceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
public class GeminiApiClient {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com";
    private static final Pattern JSON_BLOCK = Pattern.compile("```(?:json)?\\s*(.*?)\\s*```", Pattern.DOTALL);
    private static final int MAX_README_CHARS = 8000;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-3-flash-preview}")
    private String model;

    public record CoreCriterion(String label, List<String> good, List<String> bad) {}
    public record BonusItem(String label, boolean satisfied) {}

    public GeminiApiClient(ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(120_000);

        this.restClient = RestClient.builder()
                .baseUrl(GEMINI_BASE_URL)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Accept-Encoding", "identity")
                .requestFactory(factory)
                .build();
        this.objectMapper = objectMapper;
    }

    public GeminiScoreResponse scoreReadme(
            String repoName, String readmeContent,
            int readability, int visual, int reproducibilityCode, int license) {
        String prompt = buildScorePrompt(repoName, truncate(readmeContent),
                readability, visual, reproducibilityCode, license);
        return parseResponse(callGemini(prompt, 0.0), GeminiScoreResponse.class);
    }

    public GeminiSentenceResponse writeSentences(
            String repoName, String readmeContent,
            List<CoreCriterion> coreCriteria,
            List<BonusItem> bonusItems) {

        int expectedStrengths = (int) coreCriteria.stream().filter(c -> !c.good().isEmpty()).count()
                + (int) bonusItems.stream().filter(BonusItem::satisfied).count();
        int expectedImprovements = (int) coreCriteria.stream().filter(c -> !c.bad().isEmpty()).count()
                + (int) bonusItems.stream().filter(b -> !b.satisfied()).count();

        String prompt = buildSentencePrompt(repoName, truncate(readmeContent),
                coreCriteria, bonusItems, expectedStrengths, expectedImprovements);
        GeminiSentenceResponse result = parseResponse(callGemini(prompt, 0.7), GeminiSentenceResponse.class);

        if (result.strengths() != null && result.strengths().size() != expectedStrengths) {
            log.warn("strengths 개수 불일치: 기대 {} 실제 {}", expectedStrengths, result.strengths().size());
        }
        if (result.improvements() != null && result.improvements().size() != expectedImprovements) {
            log.warn("improvements 개수 불일치: 기대 {} 실제 {}", expectedImprovements, result.improvements().size());
        }
        return result;
    }

    private String truncate(String content) {
        if (content.length() <= MAX_README_CHARS) return content;
        return content.substring(0, MAX_README_CHARS) + "\n\n[... README가 너무 길어 앞 부분만 분석합니다 ...]";
    }

    private String callGemini(String prompt, double temperature) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("temperature", temperature)
        );
        String url = "/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        try {
            String response = restClient.post()
                    .uri(url)
                    .body(body)
                    .exchange((req, resp) ->
                            new String(resp.getBody().readAllBytes(), StandardCharsets.UTF_8));

            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty()) {
                JsonNode error = root.path("error");
                if (!error.isMissingNode()) {
                    log.error("Gemini API 오류 - code={} status={} message={}",
                            error.path("code").asText(),
                            error.path("status").asText(),
                            error.path("message").asText());
                } else {
                    log.error("Gemini 응답에 candidates 없음 (promptFeedback={})",
                            root.path("promptFeedback").toString());
                }
                throw new RuntimeException("Gemini API 오류: " + root.path("error").path("message").asText(response));
            }
            return candidates.get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("Gemini API 호출 실패: " + e.getMessage(), e);
        }
    }

    private <T> T parseResponse(String raw, Class<T> type) {
        String json = raw.strip();
        Matcher matcher = JSON_BLOCK.matcher(json);
        if (matcher.find()) json = matcher.group(1).strip();
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", raw);
            throw new RuntimeException("Gemini 응답 파싱 실패", e);
        }
    }

    private String buildScorePrompt(
            String repoName, String readmeContent,
            int readability, int visual, int reproducibilityCode, int license) {
        return """
                당신은 학생들의 성장을 돕는 친절하고 꼼꼼한 시니어 개발자입니다.
                다음은 GitHub 리포지토리 '%s'의 README.md 내용입니다.

                [코드 분석으로 이미 채점된 항목]
                - 가독성 (헤딩 계층·코드 블록·목록/표·헤딩 연속성): %d / 4점
                - 시각 자료 (배지 제외 이미지): %d / 1점
                - 재현성 코드분 (버전 명시·의존성 설치·실행 명령어): %d / 3점
                - 라이선스 (GitHub 라이선스 필드): %d / 1점

                [당신이 채점할 항목]

                1. clarity (명확성) — 0~4점
                아래 4개 세부 항목을 README 원문 근거와 함께 판정하세요.
                근거 문장이 없으면 반드시 미충족으로 처리하세요.
                  - 프로젝트 목적: 무엇을 해결·수행하는지 문장으로 서술됨 (제목 반복만으론 미충족)
                  - 주요 기능: 기능이 2개 이상 나열·서술됨 ("여러 기능 제공" 뭉뚱그림은 미충족)
                  - 기술 스택: 언어 또는 프레임워크명이 명시됨
                  - 사용 맥락: 대상 사용자 또는 사용 상황이 언급됨
                충족 개수 = 점수 (0~4)
                satisfied_subs에 충족된 세부 항목명을, unsatisfied_subs에 미충족 항목명을 담으세요.

                2. reproducibility_result (재현성 — 실행 결과) — 0~1점
                  1점: 출력 예시(코드 블록 안 실행 로그·샘플 결과) 또는 실행 후 기대 동작 문장 설명이 있음
                  0점: 명령어만 있고 결과·기대 동작 설명이 전혀 없음
                  규칙: 스크린샷·이미지만 있고 텍스트 설명이 없으면 0점.

                3. collaboration (협업) — 0/1점
                  1점: 따라 할 수 있는 구체적 절차 존재
                       (포크→브랜치→PR 흐름 / CONTRIBUTING 링크 / 개발환경 세팅+테스트 실행 안내 중 하나 이상)
                  0점: 없거나 "Contributions welcome" 같은 한 줄 언급뿐 (반드시 0점)

                [missing_essentials 판정]
                아래 항목 중 README에 없는 것을 배열에 담으세요.
                - 필수: 프로젝트 목적, 설치 방법, 실행 방법
                - 권장 (없으면 "(권장)" 표시 추가): 시각 자료, 협업 안내

                [제약 사항]
                순수 JSON 객체만 출력하세요. 마크다운 코드 블록(```json)을 포함하지 마세요.

                [출력 JSON 형식]
                {
                    "clarity": {
                        "score": 3,
                        "reason": "근거 한 문장",
                        "satisfied_subs": ["프로젝트 목적", "주요 기능", "기술 스택"],
                        "unsatisfied_subs": ["사용 맥락"]
                    },
                    "reproducibility_result": { "score": 1, "reason": "근거 한 문장" },
                    "collaboration":          { "score": 0, "reason": "근거 한 문장" },
                    "missing_essentials": ["누락된 항목. 모두 있다면 빈 배열"]
                }

                [README.md 내용]
                %s
                """.formatted(repoName, readability, visual, reproducibilityCode, license, readmeContent);
    }

    private String buildSentencePrompt(
            String repoName, String readmeContent,
            List<CoreCriterion> coreCriteria,
            List<BonusItem> bonusItems,
            int expectedStrengths,
            int expectedImprovements) {

        // 잘한 점 항목 목록 구성
        List<String> strengthItems = new java.util.ArrayList<>();
        for (CoreCriterion core : coreCriteria) {
            if (!core.good().isEmpty()) {
                strengthItems.add(core.label() + " (잘된 세부: " + String.join(", ", core.good()) + ")");
            }
        }
        for (BonusItem bonus : bonusItems) {
            if (bonus.satisfied()) {
                strengthItems.add(bonus.label());
            }
        }

        // 보완할 점 항목 목록 구성
        List<String> improvementItems = new java.util.ArrayList<>();
        for (CoreCriterion core : coreCriteria) {
            if (!core.bad().isEmpty()) {
                improvementItems.add(core.label() + " (부족한 세부: " + String.join(", ", core.bad()) + ")");
            }
        }
        for (BonusItem bonus : bonusItems) {
            if (!bonus.satisfied()) {
                improvementItems.add(bonus.label());
            }
        }

        String strengthBlock = IntStream.range(0, strengthItems.size())
                .mapToObj(i -> (i + 1) + ". " + strengthItems.get(i))
                .collect(Collectors.joining("\n"));
        String improvementBlock = IntStream.range(0, improvementItems.size())
                .mapToObj(i -> (i + 1) + ". " + improvementItems.get(i))
                .collect(Collectors.joining("\n"));

        return """
                당신은 학생의 GitHub README를 평가하는 친절한 시니어 개발자입니다.
                리포지토리: %s

                [절대 규칙]
                - 각 번호 항목에 대해 정확히 한 문장씩 작성합니다. 합치거나 생략하지 마세요.
                - 정중하고 친절한 존댓말, README에 실제로 있는 팩트 기반으로 구체적으로 씁니다.
                - 추상적 칭찬 금지. 내부 분류 용어(core, bonus, good, bad, 세부 등)는 출력 문장에 절대 쓰지 마세요.
                - 문장 표현은 자연스럽고 다양하게 작성하세요.

                [잘한 점 — 아래 %d개 항목 각각에 대해 한 문장씩 (strengths 배열에 순서대로)]
                %s

                [보완할 점 — 아래 %d개 항목 각각에 대해 한 문장씩 (improvements 배열에 순서대로)]
                %s

                [advice]
                보완할 점 중 우선 개선할 것을 30분 내 실행 가능한 조언으로 최대 3개.
                보완할 점이 없으면 유지·발전 관점의 조언 1개만 작성하세요.

                [출력 — 순수 JSON, 마크다운 코드블록 금지]
                {
                    "strengths":    [/* %d개 한 문장씩 */],
                    "improvements": [/* %d개 한 문장씩 */],
                    "advice":       [/* 최대 3개 */]
                }

                [README.md]
                %s
                """.formatted(
                repoName,
                expectedStrengths, strengthBlock,
                expectedImprovements, improvementBlock,
                expectedStrengths, expectedImprovements,
                readmeContent);
    }
}