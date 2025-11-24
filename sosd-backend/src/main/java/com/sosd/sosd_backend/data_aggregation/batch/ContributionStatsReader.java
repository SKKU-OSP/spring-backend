package com.sosd.sosd_backend.data_aggregation.batch;

import com.sosd.sosd_backend.data_aggregation.dto.AccountRepoProjection;
import com.sosd.sosd_backend.data_aggregation.repository.AggregationGithubAccountRepositoryLinkRepository;
import com.sosd.sosd_backend.entity.github.GithubAccountRepositoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ContributionStatsReader implements ItemReader<AccountRepoProjection> {
    private final AggregationGithubAccountRepositoryLinkRepository linkRepository;
    private Iterator<AccountRepoProjection> iterator;

    @Override
    public AccountRepoProjection read() {
        if (iterator == null) {
            List<AccountRepoProjection> links = linkRepository.findLinksNeedUpdate();
            iterator = links.iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }
}

