package com.sosd.sosd_backend.dto.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 회원가입 요청 DTO
 */
@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserSignUpRequest {

    @NotBlank
    private final String studentId;

    @NotBlank
    private final String name;

    @NotBlank
    private final String college;

    @NotBlank
    private final String dept;

    /* 복수전공 여부 */
    private final boolean pluralMajor;

    /* 휴학 졸업 여부 */
    private final boolean absence;

    /* GitHub 정보 */
    @NotNull
    private final Long githubId;

    @NotBlank
    private final String githubLoginUsername;

    private final String githubName;

    @NotBlank
    @Email
    private final String githubEmail;
}
