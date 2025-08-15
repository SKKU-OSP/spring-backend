package com.sosd.sosd_backend.github_collector.collector;

import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import com.sosd.sosd_backend.github_collector.dto.ref.RepoRef;

import java.util.List;

public interface ResourceCollectorInterface<C, P> {
    List<C> collect(GithubAccountRef githubAccountRef, RepoRef repoRef); // commit, pr, issue, star 수집 후 responseDto 반환
    List<P> persist(GithubAccountRef githubAccountRef, RepoRef repoRef, List<C> data); // 각 resource upsert 후 ref dto 반환
    String getType(); // commit, pr, issue, star
}
