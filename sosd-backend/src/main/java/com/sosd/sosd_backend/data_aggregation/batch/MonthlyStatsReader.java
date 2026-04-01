package com.sosd.sosd_backend.data_aggregation.batch;

import com.sosd.sosd_backend.data_aggregation.repository.AggregationGithubAccountRepository;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class MonthlyStatsReader implements ItemReader<GithubAccount> {

    private final AggregationGithubAccountRepository accountRepository;
    private Iterator<GithubAccount> iterator;

    @Override
    public GithubAccount read() {
        if (iterator == null) {
            List<GithubAccount> accounts = accountRepository.findAll();
            log.info("MonthlyStatsReader: {}개 계정 로드됨", accounts.size());
            iterator = accounts.iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }
}
