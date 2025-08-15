package com.sosd.sosd_backend.repository.user;

import com.sosd.sosd_backend.entity.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    // PK로 조회
    boolean existsByStudentId(String studentId);
    UserAccount findByStudentId(String studentId);

    // active user 모두 조회
    List<UserAccount> findAllByActiveTrue();

}
