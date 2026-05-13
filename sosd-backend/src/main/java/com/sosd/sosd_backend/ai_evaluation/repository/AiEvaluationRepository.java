package com.sosd.sosd_backend.ai_evaluation.repository;

import com.sosd.sosd_backend.ai_evaluation.entity.GithubRepoAiEvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiEvaluationRepository extends JpaRepository<GithubRepoAiEvaluationEntity, Long> {

    Optional<GithubRepoAiEvaluationEntity> findByGithubLoginUsernameAndRepoName(
            String githubId, String repoName);
}
