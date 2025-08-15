package com.sosd.sosd_backend.github_collector.dto.ref;

import java.util.Objects;


/**
 * 깃허브 수집 로직에서 사용할 유저 잠조 정보 DTO
 *
 * @param studentId
 * @param name
 */
public record UserAccountRef(
        String studentId,
        String name
) {
    public UserAccountRef {
        Objects.requireNonNull(studentId, "Student Id can't be null");
        Objects.requireNonNull(name, "Name can't be null");
    }
}
