package com.sosd.sosd_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory;

import javax.sql.DataSource;

@Configuration
public class JobRunrConfig {

    @Bean
    public StorageProvider storageProvider(JobMapper jobMapper, DataSource dataSource) {
        return SqlStorageProviderFactory.using(dataSource);
    }
}
