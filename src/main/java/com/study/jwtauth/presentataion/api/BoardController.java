package com.study.jwtauth.presentataion.api;

import com.study.jwtauth.infrastructure.security.CustomUserDetails;
import com.study.jwtauth.presentataion.dto.common.ApiResponse;
import com.study.jwtauth.presentataion.dto.request.CreateBoardRequest;
import com.study.jwtauth.presentataion.dto.response.BoardResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게시판 API 컨트롤러 (Access Token 테스트용)
 */
@Slf4j
@RestController
@RequestMapping("/api/boards")
public class BoardController {

    /**
     * 게시글 목록 조회 (인증 필요)
     * GET /api/boards
     */
    @GetMapping
    public ApiResponse<List<BoardResponse>> getBoards(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // API 로그는 ApiLoggingInterceptor에서 자동으로 기록 (userId, email 포함)

        // Mock 데이터 반환 (실제로는 DB 조회)
        List<BoardResponse> boards = List.of(
                BoardResponse.of(1L, "첫 번째 게시글", "내용1", 1L, "홍길동"),
                BoardResponse.of(2L, "두 번째 게시글", "내용2", 2L, "김철수"),
                BoardResponse.of(3L, "세 번째 게시글", "내용3", 1L, "홍길동")
        );

        return ApiResponse.ok(boards);
    }

    /**
     * 게시글 작성 (인증 필요)
     * POST /api/boards
     */
    @PostMapping
    public ApiResponse<BoardResponse> createBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateBoardRequest request
    ) {
        // API 로그는 ApiLoggingInterceptor에서 자동으로 기록

        // Mock 데이터 반환 (실제로는 DB 저장)
        BoardResponse board = BoardResponse.of(
                100L,
                request.title(),
                request.content(),
                userDetails.getId(),
                userDetails.getNickname()
        );

        return ApiResponse.created(board);
    }

    /**
     * 내 게시글 목록 조회 (인증 필요)
     * GET /api/boards/me
     */
    @GetMapping("/me")
    public ApiResponse<List<BoardResponse>> getMyBoards(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // API 로그는 ApiLoggingInterceptor에서 자동으로 기록

        // Mock 데이터 반환 (실제로는 DB 조회)
        List<BoardResponse> myBoards = List.of(
                BoardResponse.of(1L, "내가 쓴 글 1", "내용1", userDetails.getId(), userDetails.getNickname()),
                BoardResponse.of(3L, "내가 쓴 글 2", "내용3", userDetails.getId(), userDetails.getNickname())
        );

        return ApiResponse.ok(myBoards);
    }

    /**
     * 특정 게시글 조회 (인증 필요)
     * GET /api/boards/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<BoardResponse> getBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        // API 로그는 ApiLoggingInterceptor에서 자동으로 기록

        // Mock 데이터 반환 (실제로는 DB 조회)
        BoardResponse board = BoardResponse.of(
                id,
                "게시글 " + id,
                "게시글 " + id + "의 내용입니다.",
                1L,
                "홍길동"
        );

        return ApiResponse.ok(board);
    }
}
