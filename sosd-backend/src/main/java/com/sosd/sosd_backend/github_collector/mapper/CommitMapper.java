package com.sosd.sosd_backend.github_collector.mapper;

import com.sosd.sosd_backend.dto.github.GithubCommitUpsertDto;
import com.sosd.sosd_backend.github_collector.dto.response.GithubCommitResponseDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface CommitMapper {

    @Mapping(target = "addition", source = "src.additions")
    @Mapping(target = "deletion", source = "src.deletions")
    @Mapping(target = "authorDateUtc", expression = "java(toUtc(src.authoredDate()))")
    @Mapping(target = "committerDateUtc", expression = "java(toUtc(src.committedDate()))")
    @Mapping(target = "repositoryId", source = "repositoryId")
    @Mapping(target = "accountGithubId", source = "accountGithubId")
    GithubCommitUpsertDto toUpsertDto(GithubCommitResponseDto src,
                                      Long repositoryId,
                                      Long accountGithubId);

    /* OffsetDateTime → UTC(LocalDateTime) */
    default LocalDateTime toUtc(OffsetDateTime odt) {
        return odt == null ? null : odt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}