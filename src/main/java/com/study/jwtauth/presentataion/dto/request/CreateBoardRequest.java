package com.study.jwtauth.presentataion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBoardRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(min = 1, max = 100, message = "제목은 1자 이상 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(min = 1, max = 5000, message = "내용은 1자 이상 5000자 이하여야 합니다.")
        String content
) {
}
