package com.sosd.sosd_backend.service.auth;

import com.sosd.sosd_backend.dto.auth.SignupRequest;
import com.sosd.sosd_backend.dto.auth.SignupResponse;
import com.sosd.sosd_backend.entity.github.GithubAccount;
import com.sosd.sosd_backend.entity.user.UserAccount;
import com.sosd.sosd_backend.exception.SignUpDuplicateUserException;
import com.sosd.sosd_backend.repository.github.GithubAccountRepository;
import com.sosd.sosd_backend.repository.user.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserAccountRepository userAccountRepository;
    private final GithubAccountRepository githubAccountRepository;

    @Transactional
    public SignupResponse signup(SignupRequest request){

        // 1. 학번 중복 체크
        if (userAccountRepository.existsByStudentId(request.getStudentId())){
            throw new SignUpDuplicateUserException();
        }

        // 2. 깃허브 id 중복 체크
        if(githubAccountRepository.existsByGithubId(request.getGithubId())){
            throw new SignUpDuplicateUserException();
        }

        UserAccount user = UserAccount.builder()
                .studentId(request.getStudentId())
                .name(request.getName())
                .role(0)
                .college(request.getCollege())
                .dept(request.getDept())
                .pluralMajor(request.getPluralMajor())
                .absence(request.getAbsence())
                .isActive(true)
                .build();
        UserAccount savedUser = userAccountRepository.save(user);

        GithubAccount github = GithubAccount.builder()
                .githubId(request.getGithubId())
                .githubGraphqlNodeId(request.getGithubGraphqlNodeId())
                .githubLoginUsername(request.getGithubLoginUsername())
                .githubName(request.getGithubName())
                .githubEmail(request.getGithubEmail())
                .userAccount(savedUser)
                .build();
        githubAccountRepository.save(github);

        return SignupResponse.builder()
                .studentId(savedUser.getStudentId())
                .name(savedUser.getName())
                .college(savedUser.getCollege())
                .dept(savedUser.getDept())
                .pluralMajor(savedUser.getPluralMajor())
                .absence(savedUser.getAbsence())
                .githubId(github.getGithubId())
                .githubGraphqlNodeId(github.getGithubGraphqlNodeId())
                .githubLoginUsername(github.getGithubLoginUsername())
                .githubName(github.getGithubName())
                .githubEmail(github.getGithubEmail())
                .build();
    }
}
