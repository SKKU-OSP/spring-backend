package com.sosd.sosd_backend.repository.github;

import com.sosd.sosd_backend.entity.github.GithubStarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface GithubStarRepository extends JpaRepository<GithubStarEntity, Integer> {

    ////// 통계용 쿼리 //////
    // 특정 레포의 모든 star 개수
    Long countByRepository_Id(Long repositoryId);

    // 특정 연도의 모든 star 개수
    Long countByStarDateBetween(LocalDateTime start, LocalDateTime end);
}
