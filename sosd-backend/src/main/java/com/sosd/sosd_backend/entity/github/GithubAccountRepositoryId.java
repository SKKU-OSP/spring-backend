package com.sosd.sosd_backend.entity.github;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GithubAccountRepositoryId implements Serializable {
    @Column(name = "github_account_id")
    private Long githubAccountId;

    @Column(name = "github_repo_id")
    private Long githubRepoId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GithubAccountRepositoryId that = (GithubAccountRepositoryId) o;
        return Objects.equals(githubAccountId, that.githubAccountId) && Objects.equals(githubRepoId, that.githubRepoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(githubAccountId, githubRepoId);
    }
}
