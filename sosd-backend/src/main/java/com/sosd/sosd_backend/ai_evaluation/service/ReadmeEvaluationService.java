package com.sosd.sosd_backend.ai_evaluation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.sosd_backend.ai_evaluation.client.GeminiApiClient;
import com.sosd.sosd_backend.ai_evaluation.client.GeminiApiClient.BonusItem;
import com.sosd.sosd_backend.ai_evaluation.client.GeminiApiClient.CoreCriterion;
import com.sosd.sosd_backend.ai_evaluation.dto.AiEvaluationResponse;
import com.sosd.sosd_backend.ai_evaluation.dto.AiEvaluationResponse.CriterionScore;
import com.sosd.sosd_backend.ai_evaluation.dto.GeminiScoreResponse;
import com.sosd.sosd_backend.ai_evaluation.dto.GeminiSentenceResponse;
import com.sosd.sosd_backend.ai_evaluation.entity.GithubRepoAiEvaluationEntity;
import com.sosd.sosd_backend.ai_evaluation.util.ReadmeCodeAnalyzer;
import com.sosd.sosd_backend.ai_evaluation.util.ReadmeCodeAnalyzer.SubItemDetail;
import com.sosd.sosd_backend.ai_evaluation.util.ReadmeScoreCalculator;
import com.sosd.sosd_backend.ai_evaluation.repository.AiEvaluationRepository;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadmeEvaluationService {

    private final AiEvaluationRepository aiEvaluationRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Optional<AiEvaluationResponse> getEvaluation(String githubLoginUsername, String repoName) {
        return aiEvaluationRepository
                .findByGithubLoginUsernameAndRepoName(githubLoginUsername, repoName)
                .map(entity -> {
                    String readme = githubRepositoryRepository
                            .findByOwnerNameAndRepoName(githubLoginUsername, repoName)
                            .map(GithubRepositoryEntity::getReadme)
                            .orElse(null);
                    return AiEvaluationResponse.from(entity, readme);
                });
    }

    @Transactional
    public AiEvaluationResponse evaluate(String githubLoginUsername, String repoName) {
        // 1. DB에서 README + license 조회
        GithubRepositoryEntity repo = githubRepositoryRepository
                .findByOwnerNameAndRepoName(githubLoginUsername, repoName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "레포지토리를 찾을 수 없습니다: " + githubLoginUsername + "/" + repoName));

        if (repo.getReadme() == null || repo.getReadme().isBlank()) {
            throw new IllegalArgumentException("README가 없는 레포지토리입니다: " + repoName);
        }

        String readme = repo.getReadme();

        // 2. 코드 판정 (세부 항목까지)
        SubItemDetail readabilityDetail         = ReadmeCodeAnalyzer.readabilityDetail(readme);
        SubItemDetail reproducibilityCodeDetail = ReadmeCodeAnalyzer.reproducibilityCodeDetail(readme);
        int visual   = ReadmeCodeAnalyzer.analyzeVisual(readme);
        int license  = ReadmeCodeAnalyzer.analyzeLicense(repo.getLicense());

        int readability         = readabilityDetail.good().size();
        int reproducibilityCode = reproducibilityCodeDetail.good().size();

        log.info("코드 판정 완료: {}/{} → readability={}, visual={}, repro_code={}, license={}",
                githubLoginUsername, repoName, readability, visual, reproducibilityCode, license);

        // 3. LLM 1차 호출: 채점만 (temperature=0.0)
        log.info("LLM 채점 시작: {}/{}", githubLoginUsername, repoName);
        GeminiScoreResponse score = geminiApiClient.scoreReadme(
                repoName, readme, readability, visual, reproducibilityCode, license);

        int clarity               = score.clarity() != null ? score.clarity().score() : 0;
        int reproducibilityResult = score.reproducibilityResult() != null ? score.reproducibilityResult().score() : 0;
        int collaboration         = score.collaboration() != null ? score.collaboration().score() : 0;

        // 4. 재현성 세부: 코드분 3개 + LLM 실행결과 1개 합산
        List<String> reproGood = new ArrayList<>(reproducibilityCodeDetail.good());
        List<String> reproBad  = new ArrayList<>(reproducibilityCodeDetail.bad());
        if (reproducibilityResult == 1) reproGood.add("실행 결과"); else reproBad.add("실행 결과");

        // 5. 명확성 세부: LLM 1차 판정 결과 사용
        List<String> clarityGood = score.clarity() != null && score.clarity().satisfiedSubs() != null
                ? score.clarity().satisfiedSubs() : List.of();
        List<String> clarityBad  = score.clarity() != null && score.clarity().unsatisfiedSubs() != null
                ? score.clarity().unsatisfiedSubs() : List.of();

        // 6. 핵심 기준 + 보너스 구조 조립
        List<CoreCriterion> coreCriteria = List.of(
                new CoreCriterion("명확성", clarityGood, clarityBad),
                new CoreCriterion("재현성", reproGood, reproBad),
                new CoreCriterion("가독성", readabilityDetail.good(), readabilityDetail.bad())
        );
        List<BonusItem> bonusItems = List.of(
                new BonusItem("시각 자료", visual == 1),
                new BonusItem("라이선스", license == 1),
                new BonusItem("협업",     collaboration == 1)
        );

        // 7. LLM 2차 호출: 문장화 (temperature=0.7)
        log.info("LLM 문장화 시작: {}/{}", githubLoginUsername, repoName);
        GeminiSentenceResponse sentences = geminiApiClient.writeSentences(
                repoName, readme, coreCriteria, bonusItems);

        // 빈 배열 보정 (LLM이 빈 섹션 생성 불가한 경우 기본 문구)
        List<String> strengths = (sentences.strengths() == null || sentences.strengths().isEmpty())
                ? List.of("아직 강조할 만한 항목을 찾지 못했어요. 아래 보완할 점을 하나씩 채워가시면 좋겠습니다.")
                : sentences.strengths();
        List<String> improvements = (sentences.improvements() == null || sentences.improvements().isEmpty())
                ? List.of("모든 항목이 충실히 작성되어 현재 보완할 점은 없습니다. 훌륭합니다!")
                : sentences.improvements();

        // 8. 합산 및 등급
        double totalScore = ReadmeScoreCalculator.calculateTotal(
                clarity, readability, reproducibilityCode, reproducibilityResult,
                visual, license, collaboration);
        String grade = ReadmeScoreCalculator.toGrade(totalScore);

        // 9. criteria_scores 맵 구성
        Map<String, CriterionScore> criteriaMap = new LinkedHashMap<>();
        criteriaMap.put("clarity",                new CriterionScore(clarity,               score.clarity() != null ? score.clarity().reason() : ""));
        criteriaMap.put("readability",            new CriterionScore(readability,            "마크다운 구조 분석"));
        criteriaMap.put("reproducibility_code",   new CriterionScore(reproducibilityCode,   "버전·의존성·실행명령어 정규식 분석"));
        criteriaMap.put("reproducibility_result", new CriterionScore(reproducibilityResult, score.reproducibilityResult() != null ? score.reproducibilityResult().reason() : ""));
        criteriaMap.put("visual",                 new CriterionScore(visual,                "이미지 문법 분석"));
        criteriaMap.put("license",                new CriterionScore(license,               "GitHub 라이선스 필드 확인"));
        criteriaMap.put("collaboration",          new CriterionScore(collaboration,         score.collaboration() != null ? score.collaboration().reason() : ""));

        String criteriaJson;
        try {
            criteriaJson = objectMapper.writeValueAsString(criteriaMap);
        } catch (JsonProcessingException e) {
            log.warn("criteria_scores 직렬화 실패, null로 저장", e);
            criteriaJson = null;
        }

        // 10. DB 저장
        GithubRepoAiEvaluationEntity entity = aiEvaluationRepository
                .findByGithubLoginUsernameAndRepoName(githubLoginUsername, repoName)
                .orElseGet(() -> GithubRepoAiEvaluationEntity.builder()
                        .githubLoginUsername(githubLoginUsername)
                        .repoName(repoName)
                        .build());

        entity.updateReadmeEvaluation(
                grade, totalScore, criteriaJson,
                score.missingEssentials(), strengths, improvements, sentences.advice());

        GithubRepoAiEvaluationEntity saved = aiEvaluationRepository.save(entity);
        log.info("README 평가 완료: {}/{} → grade={}, total={}", githubLoginUsername, repoName, grade, totalScore);

        return AiEvaluationResponse.from(saved, readme);
    }
}