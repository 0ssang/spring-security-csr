package com.study.jwtauth.presentataion.api;

import com.study.jwtauth.application.service.CommentService;
import com.study.jwtauth.infrastructure.security.CustomUserDetails;
import com.study.jwtauth.presentataion.dto.common.ApiResponse;
import com.study.jwtauth.presentataion.dto.common.PageResponse;
import com.study.jwtauth.presentataion.dto.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // 사용자가 작성한 댓글 목록 조회
    @GetMapping("/my")
    public ApiResponse<PageResponse<CommentResponse>> getMyComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ){
        Long userId = userDetails.getId();
        PageResponse<CommentResponse> response = commentService.getCommentsByAuthor(userId, pageable);
        return ApiResponse.ok(response);
    }
}
