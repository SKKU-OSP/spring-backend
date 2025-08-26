package com.sosd.sosd_backend.repository.github;

import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.github.GithubPullRequestEntity;
import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GithubPullRequestRepository extends JpaRepository<GithubPullRequestEntity, Long> {

    // PR id로 단건 조회
    Optional<GithubPullRequestEntity> findByGithubPrId(Long githubPrId);

    ////// 통계 관련 쿼리 //////
    // 특정 레포의 모든 PR 개수
    Long countByRepository_Id(Long repositoryId);

    // 특정 계정의 모든 PR 개수
    Long countByAccount_GithubId(Long accountGithubId);

    // 특정 년도의 모든 PR 개수
    Long countByPrDateBetween(LocalDateTime start, LocalDateTime end);

    // 특정 사용자의 연도별 PR 개수
    Long countByAccount_GithubIdAndPrDateBetween(
            Long accountGithubId,
            LocalDateTime start,
            LocalDateTime end);

    // 특정 사용자의 특정 레포에 대한 연도별 PR 개수
    Long countByAccount_GithubIdAndRepository_idAndPrDateBetween(
            Long repositoryId,
            Long accountGithubId,
            LocalDateTime start,
            LocalDateTime end);


}
