package com.sosd.sosd_backend.github_collector.mapper;

import com.sosd.sosd_backend.dto.github.GithubStarUpsertDto;
import com.sosd.sosd_backend.github_collector.dto.response.GithubStarResponseDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface StarMapper {

    @Mapping(target = "starUserGithubId", expression = "java(extractUserId(dto.node()))")
    @Mapping(target = "starDateUtc",      expression = "java(toUtc(dto.starredAt()))")
    @Mapping(target = "repositoryId",     source = "repositoryId")
    GithubStarUpsertDto toUpsertDto(GithubStarResponseDto dto,
                                    Long repositoryId);

    // ===== helpers =====
    default Long extractUserId(GithubStarResponseDto.Node node) {
        return node == null ? null : node.id();
    }

    default LocalDateTime toUtc(OffsetDateTime odt) {
        return odt == null ? null : odt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}