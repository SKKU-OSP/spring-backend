package com.sosd.sosd_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank
    private String studentId;

    @NotBlank
    private String name;

    @NotBlank
    private String college;

    @NotBlank
    private String dept;

    private int pluralMajor;
    private int absence;

    @NotNull
    private Long githubId;

    @NotBlank
    private String githubLoginUsername;

    private String githubName;

    @NotBlank
    private String githubEmail;

}
