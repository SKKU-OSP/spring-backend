package com.sosd.sosd_backend.github_collector.orchestrator;

import com.sosd.sosd_backend.entity.github.GithubRepositoryEntity;
import com.sosd.sosd_backend.github_collector.collector.RepoCollector;
import com.sosd.sosd_backend.github_collector.dto.ref.GithubAccountRef;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GithubAccountCollectionOrchestrator {

    private final RepoCollector repoCollector;
    List<GithubRepositoryEntity> githubRepositoryEntities;

    public GithubAccountCollectionOrchestrator(RepoCollector repoCollector) {
        this.repoCollector = repoCollector;
    }

    public void collectByGithubAccount(GithubAccountRef githubAccountRef){
        // githubRepositoryEntities = collcetorRepoSomthing...
        // for (GithubRepositoryEntity githubRepositoryEntity : githubRepositoryEntities) {
        //      RepoRef repoRef = githubRepositoryEntity.toRepoRef();
        //      githubRepositoryOrchestrator.collectByRepository(githubAccountRef, repoRef)
        // }

        return;
    }
}
