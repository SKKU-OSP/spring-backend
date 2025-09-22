package com.sosd.sosd_backend.dto.user;

import java.time.LocalDateTime;

public interface RepoIdWithDate {
    Long getRepoId();
    LocalDateTime getLastCommitDate();
}
