package com.sosd.sosd_backend.data_aggregation.repository;

import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GithubRepositoryRepository extends JpaRepository<GithubRepositoryEntity, Long> {

    @Query("""
        SELECT r FROM GithubRepositoryEntity r
        WHERE r.ownerName = :owner
    """)
    List<GithubRepositoryEntity> findReposNeedUpdate(
            @Param("githubId") Long githubId,
            @Param("owner") String owner,
            @Param("year") int year);
}
