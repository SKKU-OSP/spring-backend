package com.sosd.sosd_backend.dto.auth;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SignupResponse {
    private String studentId;
    private String name;
    private String college;
    private String dept;
    private int pluralMajor;
    private int absence;
    private Long githubId;
    private String githubLoginUsername;
    private String githubName;
    private String githubEmail;
}
