package com.sosd.sosd_backend.data_aggregation.batch;

import com.sosd.sosd_backend.data_aggregation.entity.GithubContributionStats;
import com.sosd.sosd_backend.data_aggregation.repository.AggregationGithubContributionStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ContributionStatsWriter implements ItemWriter<List<GithubContributionStats>> {

    private final AggregationGithubContributionStatsRepository statsRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends List<GithubContributionStats>> chunk) {
        List<GithubContributionStats> flatList = chunk.getItems().stream()
                .flatMap(Collection::stream)
                .toList();
        statsRepository.saveAll(flatList);
    }
}
