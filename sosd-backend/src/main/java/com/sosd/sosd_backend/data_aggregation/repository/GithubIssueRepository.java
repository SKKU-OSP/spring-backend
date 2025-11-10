package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.entity.github.GithubIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubIssueRepository extends JpaRepository<GithubIssueEntity, Long> {

    @Query("""
        SELECT COUNT(i) FROM GithubIssueEntity i
        WHERE i.account = :githubId
          AND i.repository = :repoId
          AND FUNCTION('YEAR', i.issueDate) = :year
    """)
    int countByGithubIdAndRepoIdAndYear(
            @Param("githubId") Long githubId,
            @Param("repoId") Long repoId,
            @Param("year") int year);
}
