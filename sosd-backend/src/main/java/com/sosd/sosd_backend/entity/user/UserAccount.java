package com.sosd.sosd_backend.entity.user;

import com.sosd.sosd_backend.github_collector.dto.ref.UserAccountRef;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    private int absence;

    @Column(name = "is_active")
    private boolean active;

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
            boolean active
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
        this.active = active;
        this.lastLogin = null; // 로그인 시 업데이트
    }

    // 로그인 시 호출 메소드
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    // 엔티티 -> UserAccountRef 변환 메서드
    public UserAccountRef toUserRef(){
        return new UserAccountRef(this.studentId, this.name);
    }
}
