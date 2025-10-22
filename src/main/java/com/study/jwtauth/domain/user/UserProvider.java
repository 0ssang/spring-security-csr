package com.study.jwtauth.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User Aggregate 내부 Entity
 */

@Entity
@Table(
        name = "user_providers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "provider"})
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProvider implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(length = 100)
    private String providerId;

    @Column(length = 255)
    private String password;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    static UserProvider createLocalProvider(User user, String password) {
        UserProvider provider = new UserProvider();
        provider.user = user;
        provider.provider = "local";
        provider.providerId = null;
        provider.password = password;
        return provider;
    }

    public static UserProvider createOAuth2Provider(User user, String providerName, String providerId) {
        UserProvider provider = new UserProvider();
        provider.user = user;
        provider.provider = providerName;
        provider.providerId = providerId;
        provider.password = null;
        return provider;
    }
}
