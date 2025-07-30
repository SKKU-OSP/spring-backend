package com.sosd.sosd_backend.entity.user;

import com.sosd.sosd_backend.entity.github.GithubAccount;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_account")
public class UserAccount {

    // 기본 키
    @Id
    @Column(name = "student_id", length = 20)
    private String studentId; // SSO로 받은 학교 학번 사용

    // 일반 컬럼
    @Column(nullable = false, length = 40)
    private String name;

    private int role;

    @Column(nullable = false)
    private String college;

    @Column(nullable = false)
    private String dept;

    @Column(name = "plural_major")
    private int pluralMajor;

    private String photo;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(columnDefinition = "TEXT")
    private String portfolio;

    @CreationTimestamp
    @Column(name = "date_joined",  nullable = false)
    private LocalDateTime dateJoined;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    private int absence;

    @Column(name = "is_active")
    private boolean isActive;

    // 연관관계 설정
    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GithubAccount> githubAccounts;

    // 생성자
    @Builder
    public UserAccount(
            String studentId,
            String name,
            int role,
            String college,
            String dept,
            int pluralMajor,
            String photo,
            String introduction,
            String portfolio,
            int absence,
            boolean isActive
    ) {
        this.studentId = studentId;
        this.name = name;
        this.role = role;
        this.college = college;
        this.dept = dept;
        this.pluralMajor = pluralMajor;
        this.photo = photo;
        this.introduction = introduction;
        this.portfolio = portfolio;
        this.absence = absence;
        this.isActive = isActive;
        this.lastLogin = null; // 로그인 시 업데이트
    }

    // 로그인 시 호출 메소드
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
}
