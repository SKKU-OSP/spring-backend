package com.sosd.sosd_backend.github_collector.mapper;

import com.sosd.sosd_backend.dto.github.GithubPullRequestUpsertDto;
import com.sosd.sosd_backend.github_collector.dto.response.GithubPullRequestResponseDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface PullRequestMapper {

    @Mapping(target = "githubPrId",     source = "dto.databaseId")
    @Mapping(target = "prNumber",       source = "dto.number")
    @Mapping(target = "prTitle",        source = "dto.title")
    @Mapping(target = "prBody",         source = "dto.body")
    @Mapping(target = "prDateUtc",      expression = "java(toUtc(dto.createdAt()))")
    @Mapping(target = "isOpen",         expression = "java(toIsOpen(dto.state()))")
    @Mapping(target = "repositoryId",   source = "repositoryId")
    @Mapping(target = "accountGithubId",source = "accountGithubId")
    GithubPullRequestUpsertDto toUpsertDto(
            GithubPullRequestResponseDto dto,
            Long repositoryId,
            Long accountGithubId
    );

    // ===== helpers =====
    default LocalDateTime toUtc(OffsetDateTime odt) {
        return odt == null ? null : odt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    default Boolean toIsOpen(String state) {
        return state != null && state.equalsIgnoreCase("OPEN");
    }
}