package com.sosd.sosd_backend.repository.github;

import com.sosd.sosd_backend.entity.github.GithubCommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DashboardRepository extends JpaRepository<GithubCommitEntity, Long> {

    @Query(value = """
        SELECT
            COUNT(DISTINCT c.repo_id) AS repoNum,
            COUNT(c.id) AS commits,
            COALESCE(SUM(c.addition + c.deletion), 0) AS commitLines,
            (SELECT COUNT(*) FROM github_issue i WHERE i.github_id = :githubId) AS issues,
            (SELECT COUNT(*) FROM github_pull_request p WHERE p.github_id = :githubId) AS prs
        FROM github_commit c
        WHERE c.github_id = :githubId
        """, nativeQuery = true)
    Object findDashboardContr(@Param("githubId") Long githubId);
}
