package com.study.jwtauth.domain.user;

public enum Role {
    USER("일반사용자"),
    ADMIN("관리자");

    private final String description;
    Role(String description) {
        this.description = description;
    }

    public String geDescription() {
        return description;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
