package com.study.jwtauth.presentataion.dto.response;

import com.study.jwtauth.domain.user.Role;
import com.study.jwtauth.domain.user.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
