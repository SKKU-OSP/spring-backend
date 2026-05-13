package com.sosd.sosd_backend.ai_evaluation.service;

import com.sosd.sosd_backend.ai_evaluation.client.GeminiApiClient;
import com.sosd.sosd_backend.ai_evaluation.dto.AiEvaluationResponse;
import com.sosd.sosd_backend.ai_evaluation.dto.ReadmeEvaluationResult;
import com.sosd.sosd_backend.ai_evaluation.entity.GithubRepoAiEvaluationEntity;
import com.sosd.sosd_backend.ai_evaluation.repository.AiEvaluationRepository;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.repository.github.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadmeEvaluationService {

    private final AiEvaluationRepository aiEvaluationRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final GeminiApiClient geminiApiClient;

    /** 저장된 평가 결과 조회 */
    @Transactional(readOnly = true)
    public Optional<AiEvaluationResponse> getEvaluation(String githubLoginUsername, String repoName) {
        return aiEvaluationRepository
                .findByGithubLoginUsernameAndRepoName(githubLoginUsername, repoName)
                .map(AiEvaluationResponse::from);
    }

    /** DB의 README로 평가 실행 후 저장 */
    @Transactional
    public AiEvaluationResponse evaluate(String githubLoginUsername, String repoName) {
        // 1. DB에서 README 조회
        GithubRepositoryEntity repo = githubRepositoryRepository
                .findByOwnerNameAndRepoName(githubLoginUsername, repoName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "레포지토리를 찾을 수 없습니다: " + githubLoginUsername + "/" + repoName));

        if (repo.getReadme() == null || repo.getReadme().isBlank()) {
            throw new IllegalArgumentException("README가 없는 레포지토리입니다: " + repoName);
        }

        // 2. Gemini API 호출
        log.info("README 평가 시작: {}/{}", githubLoginUsername, repoName);
        ReadmeEvaluationResult result = geminiApiClient.evaluateReadme(repoName, repo.getReadme());

        // 3. DB 저장 또는 업데이트
        GithubRepoAiEvaluationEntity entity = aiEvaluationRepository
                .findByGithubLoginUsernameAndRepoName(githubLoginUsername, repoName)
                .orElseGet(() -> GithubRepoAiEvaluationEntity.builder()
                        .githubLoginUsername(githubLoginUsername)
                        .repoName(repoName)
                        .build());

        entity.updateReadmeEvaluation(
                result.score(),
                result.missingEssentials(),
                result.strengths(),
                result.improvements(),
                result.advice()
        );

        GithubRepoAiEvaluationEntity saved = aiEvaluationRepository.save(entity);
        log.info("README 평가 완료: {}/{} → score={}", githubLoginUsername, repoName, result.score());

        return AiEvaluationResponse.from(saved);
    }
}
