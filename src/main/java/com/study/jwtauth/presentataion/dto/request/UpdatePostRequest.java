package com.study.jwtauth.presentataion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 게시글 수정 요청 DTO
 */
public record UpdatePostRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(min = 1, max = 10000, message = "내용은 1자 이상 10000자 이하여야 합니다.")
        String content
) {
}