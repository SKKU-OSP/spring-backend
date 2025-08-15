package com.sosd.sosd_backend.service.auth;

import com.sosd.sosd_backend.dto.auth.SignupRequest;
import com.sosd.sosd_backend.dto.auth.SignupResponse;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.user.UserAccount;
import com.sosd.sosd_backend.exception.SignUpDuplicateUserException;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.repository.user.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class AuthServiceTest {

    @Autowired AuthService authService;
    @Autowired
    UserAccountRepository userRepo;
    @Autowired
    GithubAccountRepository ghRepo;

    @AfterEach
    void clean() {
        ghRepo.deleteAll();
        userRepo.deleteAll();
    }

    private SignupRequest req(String sid, long ghId) {
        var r = new SignupRequest();
        r.setStudentId(sid);
        r.setName("홍길동");
        r.setCollege("소프트웨어대학");
        r.setDept("소프트웨어학과");
        r.setPluralMajor(0);
        r.setAbsence(0);
        r.setGithubId(ghId);
        r.setGithubLoginUsername("honggit");
        r.setGithubName("Hong");
        r.setGithubEmail("hong@example.com");
        return r;
    }

    @Test
    void signup_success_persists_both() {
        SignupResponse res = authService.signup(req("20250001", 900L));

        UserAccount user = userRepo.findById("20250001").orElseThrow();
        GithubAccount gh = ghRepo.findById(900L).orElseThrow();

        assertThat(user.getName()).isEqualTo("홍길동");
        assertThat(gh.getUserAccount().getStudentId()).isEqualTo("20250001");
        assertThat(res.getGithubLoginUsername()).isEqualTo("honggit");
    }

    @Test
    void signup_duplicate_throws_and_rollback() {
        authService.signup(req("20250001", 900L));

        assertThatThrownBy(() -> authService.signup(req("20250001", 901L)))
                .isInstanceOf(SignUpDuplicateUserException.class);

        assertThat(ghRepo.findById(901L)).isEmpty(); // 롤백 확인
    }
}
