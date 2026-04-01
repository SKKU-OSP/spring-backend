package com.sosd.sosd_backend.data_aggregation.batch;

import com.sosd.sosd_backend.data_aggregation.entity.GithubMonthlyStats;
import com.sosd.sosd_backend.data_aggregation.repository.MonthlyStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyStatsWriter implements ItemWriter<List<GithubMonthlyStats>> {

    private final MonthlyStatsRepository monthlyStatsRepository;

    @Override
    public void write(Chunk<? extends List<GithubMonthlyStats>> chunk) {
        List<GithubMonthlyStats> flatList = chunk.getItems().stream()
                .flatMap(Collection::stream)
                .toList();

        for (GithubMonthlyStats item : flatList) {
            monthlyStatsRepository.upsert(item);
        }
    }
}
