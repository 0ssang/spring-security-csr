package com.study.jwtauth.application.service;

import com.study.jwtauth.domain.post.Post;
import com.study.jwtauth.domain.post.PostRepository;
import com.study.jwtauth.domain.post.PostStatus;
import com.study.jwtauth.domain.post.exception.PostNotFoundException;
import com.study.jwtauth.domain.postlike.PostLike;
import com.study.jwtauth.domain.postlike.PostLikeRepository;
import com.study.jwtauth.domain.postlike.exception.AlreadyLikedException;
import com.study.jwtauth.domain.postlike.exception.PostLikeNotFoundException;
import com.study.jwtauth.domain.user.User;
import com.study.jwtauth.domain.user.UserRepository;
import com.study.jwtauth.presentataion.dto.common.PageResponse;
import com.study.jwtauth.presentataion.dto.response.PostLikeResponse;
import com.study.jwtauth.presentataion.dto.response.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글에 좋아요 추가
    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(PostNotFoundException::new);

        if(postLikeRepository.existsByPostIdAndUserId(postId, userId)){
            throw new AlreadyLikedException();
        }

        PostLike postLike = PostLike.create(postId, userId);
        postLikeRepository.save(postLike);

        post.incrementLikeCount();
    }

    // 게시글 좋아요 취소
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(PostLikeNotFoundException::new);

        postLikeRepository.delete(postLike);

        postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .ifPresent(Post::decrementLikeCount);
    }

    // 특정 게시물의 좋아요 여부 확인
    public boolean isLiked(Long postId, Long userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    // 게시글에 좋아요 누른 사용자 목록 조회
    public PageResponse<PostLikeResponse> getPostLikes(Long postId, Pageable pageable) {
        Page<PostLike> postLikes = postLikeRepository.findByPostId(postId, pageable);
        return convertToPostLikePageResponse(postLikes);
    }

    // 내가 좋아요한 게시글 목록 조회
    public PageResponse<PostResponse> getLikedPosts(Long userId, Pageable pageable) {
        Page<PostLike> postLikes = postLikeRepository.findByUserIdWithActivePost(userId, pageable);
        return convertToPostPageResponse(postLikes);
    }

    // 헬퍼 - Page<PostLike>를 PageResponse<PostLikeResponse>로 변환
    private PageResponse<PostLikeResponse> convertToPostLikePageResponse(Page<PostLike> postLikePage) {
        List<PostLike> postLikes = postLikePage.getContent();

        List<Long> userIds = postLikes
                .stream()
                .map(PostLike::getUserId)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<PostLikeResponse> postLikeResponses = postLikes
                .stream()
                .map(postLike -> {
                    User user = userMap.get(postLike.getUserId());
                    return PostLikeResponse.of(postLike, user);
                })
                .toList();

        return new PageResponse<>(
                postLikeResponses,
                postLikePage.getNumber(),
                postLikePage.getSize(),
                postLikePage.getTotalElements(),
                postLikePage.getTotalPages(),
                postLikePage.isFirst(),
                postLikePage.isLast(),
                postLikePage.hasNext(),
                postLikePage.hasPrevious()
        );
    }

    // 헬퍼 - Page<PostLike>를 PageResponse<PostResponse>로 변환
    private PageResponse<PostResponse> convertToPostPageResponse(Page<PostLike> postLikePage) {
        List<PostLike> postLikes = postLikePage.getContent();

        List<Long> postIds = postLikes
                .stream()
                .map(PostLike::getPostId)
                .distinct()
                .toList();

        Map<Long, Post> postMap = postRepository.findAllById(postIds)
                .stream()
                .collect(Collectors.toMap(Post::getId, post -> post));

        List<Long> authorIds = postMap.values()
                .stream()
                .map(Post::getAuthorId)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(authorIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<PostResponse> postResponses = postLikes
                .stream()
                .map(postLike -> postMap.get(postLike.getPostId()))
                .map(post -> {
                    User user = userMap.get(post.getAuthorId());
                    return PostResponse.of(post, user);
                })
                .toList();

        return new PageResponse<>(
                postResponses,
                postLikePage.getNumber(),
                postLikePage.getSize(),
                postLikePage.getTotalElements(),
                postLikePage.getTotalPages(),
                postLikePage.isFirst(),
                postLikePage.isLast(),
                postLikePage.hasNext(),
                postLikePage.hasPrevious()
        );
    }
}
