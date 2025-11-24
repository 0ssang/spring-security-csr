package com.study.jwtauth.application.service;

import com.study.jwtauth.domain.comment.Comment;
import com.study.jwtauth.domain.comment.CommentRepository;
import com.study.jwtauth.domain.comment.CommentStatus;
import com.study.jwtauth.domain.comment.exception.CommentNotFoundException;
import com.study.jwtauth.domain.comment.exception.InvalidCommentDepthException;
import com.study.jwtauth.domain.post.Post;
import com.study.jwtauth.domain.post.PostRepository;
import com.study.jwtauth.domain.post.PostStatus;
import com.study.jwtauth.domain.post.exception.PostNotFoundException;
import com.study.jwtauth.domain.user.User;
import com.study.jwtauth.domain.user.UserRepository;
import com.study.jwtauth.presentataion.dto.common.PageResponse;
import com.study.jwtauth.presentataion.dto.request.CreateCommentRequest;
import com.study.jwtauth.presentataion.dto.response.CommentResponse;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 댓글 작성
    @Transactional
    public CommentResponse createComment(Long postId, CreateCommentRequest request, Long userId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new PostNotFoundException("삭제되었거나 존재하지 않는 게시글입니다."));

        Comment comment = Comment.createComment(request.content(), postId, userId);
        Comment savedComment = commentRepository.save(comment);

        User author = userRepository.findById(userId).orElse(null);
        return CommentResponse.of(savedComment, author, 0L);
    }

    // 답글 작성
    @Transactional
    public CommentResponse createReply(Long postId, Long parentCommentId, CreateCommentRequest request, Long userId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new PostNotFoundException("삭제되었거나 존재하지 않는 게시글 입니다."));

        Comment parentComment = commentRepository.findByIdAndStatus(parentCommentId, CommentStatus.ACTIVE)
                .orElseThrow(() -> new CommentNotFoundException("원 댓글이 존재하지 않습니다."));

        if(parentComment.isReply()){
            throw new InvalidCommentDepthException();
        }

        Comment comment = Comment.createReply(request.content(), postId, userId, parentCommentId);
        Comment savedComment = commentRepository.save(comment);

        User author = userRepository.findById(userId).orElse(null);
        return CommentResponse.of(savedComment, author, 0L);
    }

    // 게시글의 댓글 목록 조회
    public PageResponse<CommentResponse> getCommentsByPostId(Long postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findCommentsByPostIdAndStatus(postId, CommentStatus.ACTIVE, pageable);

        return convertToPageResponseWithReplyCount(comments);
    }

    // 댓글의 답글 목록 조회
    public PageResponse<CommentResponse> getRepliesByCommentId(Long commentId, Pageable pageable) {
        commentRepository.findByIdAndStatus(commentId, CommentStatus.ACTIVE)
                .orElseThrow(CommentNotFoundException::new);

        Page<Comment> replies = commentRepository.findRepliesByParentCommentIdAndStatus(commentId, CommentStatus.ACTIVE, pageable);

        return convertToPageResponse(replies);
    }

    // 사용자의 댓글/답글 목록 조회
    public PageResponse<CommentResponse> getCommentsByAuthor(Long authorId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findCommentsByAuthorIdAndStatus(authorId, CommentStatus.ACTIVE, pageable);

        return convertToPageResponse(comments);
    }

    // 댓글/답글 상세 조회
    public CommentResponse getComment(Long commentId) {
        Comment comment = commentRepository.findByIdAndStatus(commentId, CommentStatus.ACTIVE)
                .orElseThrow(CommentNotFoundException::new);

        User author = userRepository.findById(comment.getAuthorId()).orElse(null);

        Long replyCount = comment.isComment()
                ? commentRepository.countByParentCommentIdAndStatus(commentId, CommentStatus.ACTIVE)
                : 0L;

        return CommentResponse.of(comment, author, replyCount);
    }

    // 댓글/답글 수정
    @Transactional
    public CommentResponse updateComment(Long commentId, CreateCommentRequest request, Long userId) {
        Comment comment = commentRepository.findByIdAndStatus(commentId, CommentStatus.ACTIVE)
                .orElseThrow(CommentNotFoundException::new);

        comment.update(request.content(), userId);

        User user = userRepository.findById(userId).orElse(null);
        Long replyCount = comment.isComment()
                ? commentRepository.countByParentCommentIdAndStatus(commentId, CommentStatus.ACTIVE)
                : 0L;

        return CommentResponse.of(comment, user, replyCount);
    }

    // 댓글/답글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndStatus(commentId, CommentStatus.ACTIVE)
                .orElseThrow(CommentNotFoundException::new);

        comment.delete(userId);
    }

    // Page<Comment>를 PageResponse<CommentResponse>로 변환 (답글 개수 포함)
    private PageResponse<CommentResponse> convertToPageResponseWithReplyCount(Page<Comment> commentPage) {
        List<Comment> comments = commentPage.getContent();

        List<Long> authorIds = comments.stream()
                .map(Comment::getAuthorId)
                .distinct()
                .toList();


        Map<Long, User> userMap = userRepository.findAllById(authorIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Map<Long, Long> replyCountMap = comments.stream()
                .collect(Collectors.toMap(
                        Comment::getId,
                        comment -> commentRepository.countByParentCommentIdAndStatus(
                                comment.getId(),
                                CommentStatus.ACTIVE
                        )
                ));

        List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> {
                    User author = userMap.get(comment.getAuthorId());
                    Long replyCount = replyCountMap.get(comment.getId());
                    return CommentResponse.of(comment, author, replyCount);
                })
                .toList();

        return new PageResponse<>(
                commentResponses,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.isFirst(),
                commentPage.isLast(),
                commentPage.hasNext(),
                commentPage.hasPrevious()
        );
    }

    // Page<Comment>를 PageResponse<CommentResponse>로 변환 (답글 개수 없음)
    private PageResponse<CommentResponse> convertToPageResponse(Page<Comment> commentPage) {
        List<Comment> comments = commentPage.getContent();

        List<Long> authorIds = comments.stream()
                .map(Comment::getAuthorId)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(authorIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> {
                    User author = userMap.get(comment.getAuthorId());
                    return CommentResponse.of(comment, author, 0L);
                })
                .toList();

        return new PageResponse<>(
                commentResponses,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.isFirst(),
                commentPage.isLast(),
                commentPage.hasNext(),
                commentPage.hasPrevious()
        );
    }
}
