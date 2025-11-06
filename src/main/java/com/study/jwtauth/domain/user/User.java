package com.study.jwtauth.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name="users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<UserProvider> providers = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    public User(String email, String nickname, Role role) {
        validateEmail(email);
        validateNickname(nickname);

        this.email = email;
        this.nickname = nickname;
        this.role = role != null ? role : Role.USER;
    }

    // Factory
    public static User createUser(String email, String rawPassword, String nickname, PasswordEncoder passwordEncoder) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .role(Role.USER)
                .build();
        UserProvider localProvider = UserProvider.createLocalProvider(user, passwordEncoder.encode(rawPassword));
        user.providers.add(localProvider);

        return user;
    }

    public static User createAdmin(String email, String rawPassword, String nickname, PasswordEncoder passwordEncoder) {
        User admin = User.builder()
                .email(email)
                .nickname(nickname)
                .role(Role.ADMIN)
                .build();

        UserProvider localProvider = UserProvider.createLocalProvider(admin, passwordEncoder.encode(rawPassword));
        admin.providers.add(localProvider);

        return admin;
    }

    public static User createOidcUser(String email, String nickname, String provider, String providerId) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .role(Role.USER)
                .build();

        UserProvider oidcProvider = UserProvider.createOidcProvider(user, provider, providerId);
        user.providers.add(oidcProvider);

        return user;
    }

    public void updateOidcInfo(String nickname) {
        validateNickname(nickname);
        this.nickname = nickname;
    }

    public void addProvider(String provider, String providerId) {
        UserProvider oidcProvider = UserProvider.createOidcProvider(this, provider, providerId);
        this.providers.add(oidcProvider);
    }


    // Utils
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new com.study.jwtauth.domain.user.exception.InvalidEmailFormatException(
                com.study.jwtauth.domain.exception.ErrorCode.EMAIL_REQUIRED
            );
        }
        if(!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")){
            throw new com.study.jwtauth.domain.user.exception.InvalidEmailFormatException(
                com.study.jwtauth.domain.exception.ErrorCode.INVALID_EMAIL_FORMAT,
                email
            );
        }
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new com.study.jwtauth.domain.user.exception.InvalidNicknameException(
                com.study.jwtauth.domain.exception.ErrorCode.NICKNAME_REQUIRED
            );
        }
        if (nickname.length() < 2 || nickname.length() > 20) {
            throw new com.study.jwtauth.domain.user.exception.InvalidNicknameException(
                com.study.jwtauth.domain.exception.ErrorCode.INVALID_NICKNAME_LENGTH,
                nickname
            );
        }
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        User user = (User)o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

}
