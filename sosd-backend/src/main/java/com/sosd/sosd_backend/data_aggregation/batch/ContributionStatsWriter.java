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
    public void write(Chunk<? extends List<GithubContributionStats>> chunk) {
        // 1. Chunk<List<Stats>> 구조를 List<Stats>로 평탄화 (flatten)
        List<GithubContributionStats> flatList = chunk.getItems().stream()
                .flatMap(Collection::stream)
                .toList();

        // 2. 하나씩 순회하며 Upsert 실행
        // (ID 조회 없이 DB가 알아서 Insert/Update 판단)
        for (GithubContributionStats item : flatList) {
            statsRepository.upsert(item);
        }
    }
}
