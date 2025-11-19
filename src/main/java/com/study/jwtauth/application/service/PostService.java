package com.study.jwtauth.application.service;

import com.study.jwtauth.domain.post.Post;
import com.study.jwtauth.domain.post.PostRepository;
import com.study.jwtauth.domain.post.PostStatus;
import com.study.jwtauth.domain.post.exception.PostAccessDeniedException;
import com.study.jwtauth.domain.post.exception.PostNotFoundException;
import com.study.jwtauth.domain.user.User;
import com.study.jwtauth.domain.user.UserRepository;
import com.study.jwtauth.domain.user.exception.UserNotFoundException;
import com.study.jwtauth.presentataion.dto.common.PageResponse;
import com.study.jwtauth.presentataion.dto.request.CreatePostRequest;
import com.study.jwtauth.presentataion.dto.request.UpdatePostRequest;
import com.study.jwtauth.presentataion.dto.response.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 생성
    @Transactional
    public PostResponse createPost(CreatePostRequest request, Long authorId) {
        Post savedPost = postRepository.save(
                Post.create(request.title(), request.content(), authorId)
        );

        User user = userRepository.findById(authorId)
                .orElseThrow(UserNotFoundException::new);

        return PostResponse.of(savedPost, user.getNickname());
    }

    // 게시글 상세 조회
    @Transactional
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(PostNotFoundException::new);

        post.incrementViewCount();

        User author = userRepository.findById(post.getAuthorId())
                .orElseThrow(UserNotFoundException::new);

        return PostResponse.of(post, author.getNickname());
    }

    // 전체 작성글 조회
    public PageResponse<PostResponse> getPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.ACTIVE, pageable);
        return convertToPageResponse(posts);
    }

    // 내 작성글 조회
    public PageResponse<PostResponse> getMyPosts(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByAuthorIdAndStatusOrderByCreatedAtDesc(userId, PostStatus.ACTIVE, pageable);
        return convertToPageResponse(posts);
    }

    // 인기글 조회
    public PageResponse<PostResponse> getMostViewedPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findMostViewedPosts(pageable);
        return convertToPageResponse(posts);
    }

    // 게시글 수정
    @Transactional
    public PostResponse updatePost(Long postId, UpdatePostRequest request, Long userId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(PostNotFoundException::new);

        post.update(request.title(), request.content(), userId);

        User author = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return PostResponse.of(post, author);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(PostNotFoundException::new);

        post.delete(userId);
    }


    private PageResponse<PostResponse> convertToPageResponse(Page<Post> postPage) {
        List<Post> posts = postPage.getContent();
        List<Long> authorIds = posts
                .stream()
                .map(Post::getAuthorId)
                .distinct()
                .toList();

        Map<Long, User> authorMap = userRepository.findAllById(authorIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<PostResponse> postResponses = posts.stream()
                .map(post -> {
                    User author = authorMap.get(post.getAuthorId());
                    return PostResponse.of(post, author);
                })
                .toList();

        return new PageResponse<>(
                postResponses,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isFirst(),
                postPage.isLast(),
                postPage.hasNext(),
                postPage.hasPrevious()
        );
    }
}