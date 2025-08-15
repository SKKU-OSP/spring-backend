package com.sosd.sosd_backend.repository.user;

import com.sosd.sosd_backend.entity.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    boolean existsByStudentId(String studentId);
}
