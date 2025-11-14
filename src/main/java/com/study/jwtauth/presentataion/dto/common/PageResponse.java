package com.study.jwtauth.presentataion.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이지네이션 응답 DTO (Page용)
 * - 전체 페이지 정보 포함
 * - 페이지 번호 UI에 적합
 */
public record PageResponse<T>(
        List<T> content,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}