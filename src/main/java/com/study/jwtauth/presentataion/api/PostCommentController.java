package com.study.jwtauth.presentataion.api;

import com.study.jwtauth.application.service.CommentService;
import com.study.jwtauth.infrastructure.security.CustomUserDetails;
import com.study.jwtauth.presentataion.dto.common.ApiResponse;
import com.study.jwtauth.presentataion.dto.common.PageResponse;
import com.study.jwtauth.presentataion.dto.request.CreateCommentRequest;
import com.study.jwtauth.presentataion.dto.request.UpdateCommentRequest;
import com.study.jwtauth.presentataion.dto.response.CommentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {
    private final CommentService commentService;

    // 댓글 작성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long userId = userDetails.getId();
        CommentResponse commentResponse = commentService.createComment(postId, request, userId);
        return ApiResponse.created(commentResponse);
    }

    // 답글 작성
    @PostMapping("/{commentId}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> createReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long userId = userDetails.getId();
        CommentResponse commentResponse = commentService.createReply(postId, commentId, request, userId);
        return ApiResponse.created(commentResponse);
    }

    // 댓글/답글 상세 조회
    @GetMapping("/{commentId}")
    public ApiResponse<CommentResponse> getComment(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ){
        CommentResponse commentResponse = commentService.getComment(commentId);
        return ApiResponse.ok(commentResponse);
    }

    // 특정 게시글의 댓글 목록 조회
    @GetMapping
    public ApiResponse<PageResponse<CommentResponse>> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 20) Pageable pageable
    ){
        PageResponse<CommentResponse> response = commentService.getCommentsByPostId(postId, pageable);
        return ApiResponse.ok(response);
    }

    // 특정 댓글의 답글 목록 조회
    @GetMapping("/{commentId}/replies")
    public ApiResponse<PageResponse<CommentResponse>> getReplies(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @PageableDefault(size = 20) Pageable pageable
    ){
        PageResponse<CommentResponse> response = commentService.getRepliesByCommentId(commentId, pageable);
        return ApiResponse.ok(response);
    }

    // 댓글/답글 수정
    @PutMapping("/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long userId = userDetails.getId();
        CommentResponse response = commentService.updateComment(commentId, request, userId);
        return ApiResponse.ok(response);
    }

    // 댓글/답글 삭제
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long userId = userDetails.getId();
        commentService.deleteComment(commentId, userId);
        return ApiResponse.ok(null);
    }
}
