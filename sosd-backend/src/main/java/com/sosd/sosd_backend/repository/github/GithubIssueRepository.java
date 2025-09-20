package com.sosd.sosd_backend.repository.github;

import com.sosd.sosd_backend.entity.github.GithubIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GithubIssueRepository extends JpaRepository<GithubIssueEntity, Long> {

    // issue id로 단건 조회
    Optional<GithubIssueEntity> findByGithubIssueId(Long githubIssueId);

    List<GithubIssueEntity> findAllByRepository_IdAndGithubIssueIdIn(Long repositoryId, Collection<Long> githubIssueIds);

    ////// 통계 관련 쿼리 //////
    // 특정 레포의 모든 issue 개수
    Long countByRepository_Id(Long repositoryId);

    // 특정 계정의 모든 issue 개수
    Long countByAccount_GithubId(Long accountGithubId);

    // 특정 년도의 모든 issue 개수
    Long countByIssueDateBetween(LocalDateTime start, LocalDateTime end);

    // 특정 사용자의 연도별 issue 개수
    Long countByAccount_GithubIdAndIssueDateBetween(
            Long accountGithubId,
            LocalDateTime start,
            LocalDateTime end);

    // 특정 사용자의 특정 레포에 대한 연도별 issue 개수
    Long countByAccount_GithubIdAndRepository_idAndIssueDateBetween(
            Long repositoryId,
            Long accountGithubId,
            LocalDateTime start,
            LocalDateTime end);

}
