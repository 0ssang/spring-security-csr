package com.study.jwtauth.presentataion.api;

import com.study.jwtauth.application.service.PostService;
import com.study.jwtauth.infrastructure.security.CustomUserDetails;
import com.study.jwtauth.presentataion.dto.common.ApiResponse;
import com.study.jwtauth.presentataion.dto.common.PageResponse;
import com.study.jwtauth.presentataion.dto.request.CreatePostRequest;
import com.study.jwtauth.presentataion.dto.request.UpdatePostRequest;
import com.study.jwtauth.presentataion.dto.response.PostResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 API 컨트롤러
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 작성
     * */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        PostResponse response = postService.createPost(request, userId);
        return ApiResponse.created(response);
    }

    /**
     * 게시글 목록 조회 (전체, 최신순, 페이징)
     */
    @GetMapping
    public ApiResponse<PageResponse<PostResponse>> getPosts(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PageResponse<PostResponse> response = postService.getPosts(pageable);
        return ApiResponse.ok(response);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{id}")
    public ApiResponse<PostResponse> getPost(@PathVariable Long id) {
        PostResponse response = postService.getPost(id);
        return ApiResponse.ok(response);
    }

    /**
     * 내가 쓴 글 조회
     */
    @GetMapping("/my")
    public ApiResponse<PageResponse<PostResponse>> getMyPosts(
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        PageResponse<PostResponse> response = postService.getMyPosts(userId, pageable);
        return ApiResponse.ok(response);
    }

    /**
     * 인기글 조회 (조회수 기준)
     */
    @GetMapping("/popular")
    public ApiResponse<PageResponse<PostResponse>> getMostViewedPosts(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PageResponse<PostResponse> response = postService.getMostViewedPosts(pageable);
        return ApiResponse.ok(response);
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/{id}")
    public ApiResponse<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        PostResponse response = postService.updatePost(id, request, userId);
        return ApiResponse.ok(response);
    }

    /**
     * 게시글 삭제 (Soft Delete)
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        postService.deletePost(id, userId);
        return ApiResponse.ok(null);
    }
}
