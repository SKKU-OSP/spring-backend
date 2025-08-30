package com.sosd.sosd_backend.github_collector.mapper;

import com.sosd.sosd_backend.dto.github.GithubIssueUpsertDto;
import com.sosd.sosd_backend.github_collector.dto.response.GithubIssueResponseDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface IssueMapper {

    @Mapping(target = "githubIssueId",   source = "dto.databaseId")
    @Mapping(target = "issueNumber",     source = "dto.number")
    @Mapping(target = "issueTitle",      source = "dto.title")
    @Mapping(target = "issueBody",       source = "dto.body")
    @Mapping(target = "issueDateUtc",    expression = "java(toUtc(dto.createdAt()))")
    @Mapping(target = "repositoryId",    source = "repositoryId")
    @Mapping(target = "accountGithubId", source = "accountGithubId")
    GithubIssueUpsertDto toUpsertDto(
            GithubIssueResponseDto dto,
            Long repositoryId,
            Long accountGithubId
    );

    // ===== helpers =====
    default LocalDateTime toUtc(OffsetDateTime odt) {
        return odt == null ? null : odt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}