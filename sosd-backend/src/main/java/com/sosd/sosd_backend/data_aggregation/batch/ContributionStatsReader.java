package com.sosd.sosd_backend.data_aggregation.batch;

import com.sosd.sosd_backend.data_aggregation.repository.GithubAccountRepositoryLinkRepository;
import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ContributionStatsReader implements ItemReader<GithubAccountRepositoryEntity> {
    private final GithubAccountRepositoryLinkRepository linkRepository;
    private Iterator<GithubAccountRepositoryEntity> iterator;

    @Override
    public GithubAccountRepositoryEntity read() {
        if (iterator == null) {
            List<GithubAccountRepositoryEntity> links = linkRepository.findLinksNeedUpdate();
            iterator = links.iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }
}

