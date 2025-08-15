package com.sosd.sosd_backend.repository.github;

import com.sosd.sosd_backend.entity.github.GithubAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubAccountRepository extends JpaRepository<GithubAccount, Long> {

    // PK로 조회
    boolean existsByGithubId(Long githubId);
    Optional<GithubAccount> findByGithubId(Long githubId);

    // 유저네임으로 조회
    boolean existsByGithubLoginUsername(String githubLoginUsername);
    Optional<GithubAccount> findByGithubLoginUsername(String githubLoginUsername);

    // FK - 학번 기준 조회
    List<GithubAccount> findAllByUserAccount_StudentId(String studentId);

}