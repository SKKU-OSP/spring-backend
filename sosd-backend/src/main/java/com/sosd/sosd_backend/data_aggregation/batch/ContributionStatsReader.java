package com.sosd.sosd_backend.data_aggregation.batch;

import com.sosd.sosd_backend.data_aggregation.dto.AccountRepoProjection;
import com.sosd.sosd_backend.data_aggregation.repository.AggregationGithubAccountRepositoryLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * Step-scoped Reader로 매 Job 실행 시 새 인스턴스 생성
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class ContributionStatsReader implements ItemReader<AccountRepoProjection> {
    private final AggregationGithubAccountRepositoryLinkRepository linkRepository;
    private Iterator<AccountRepoProjection> iterator;

    @Override
    public AccountRepoProjection read() {
        if (iterator == null) {
            // 모든 account-repo 링크를 조회하여 집계 (매번 전체 재계산)
            List<AccountRepoProjection> links = linkRepository.findAllLinks();
            log.info("ContributionStatsReader: {} 개의 account-repo 링크 로드됨", links.size());
            iterator = links.iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }
}

