package com.sosd.sosd_backend.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.sosd_backend.dto.auth.SignupRequest;
import com.sosd.sosd_backend.dto.auth.SignupResponse;
import com.sosd.sosd_backend.exception.SignUpDuplicateUserException;
import com.sosd.sosd_backend.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    AuthService authService;

    private SignupRequest req(){
        SignupRequest r = new SignupRequest();
        r.setStudentId("2025111111");
        r.setName("테스트");
        r.setCollege("소프트웨어대학");
        r.setDept("소프트웨어학과");
        r.setPluralMajor(0);
        r.setAbsence(0);
        r.setGithubId(123456L);
        r.setGithubLoginUsername("githubloginusername");
        r.setGithubName("githubname");
        r.setGithubEmail("githubemail@example.com");
        return r;
    }

    @Test
    void signup_success() throws Exception {
        var res = SignupResponse.builder()
                .studentId("2025111111").name("테스트")
                .college("소프트웨어대학").dept("소프트웨어학과")
                .pluralMajor(0).absence(0).githubId(123456L)
                .githubLoginUsername("githubloginusername").githubName("githubname")
                .githubEmail("githubemail@example.com").build();
        Mockito.when(authService.signup(any(SignupRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req())))
                .andExpect(jsonPath("$.studentId").value("2025111111"))
                .andExpect(jsonPath("$.githubLoginUsername").value("githubloginusername"));
    }

    @Test
    void signup_duplicate_user() throws Exception{
        Mockito.when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new SignUpDuplicateUserException());

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.path").value("/api/auth/signup"));
    }


}
