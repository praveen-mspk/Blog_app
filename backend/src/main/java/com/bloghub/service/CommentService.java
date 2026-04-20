package com.bloghub.service;

import com.bloghub.dto.CommentRequest;
import com.bloghub.dto.CommentResponse;
import com.bloghub.dto.UserResponse;
import com.bloghub.entity.Comment;
import com.bloghub.entity.CommentLike;
import com.bloghub.entity.Post;
import com.bloghub.entity.User;
import com.bloghub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        User user = getCurrentUser();
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .user(user)
                .parent(parent)
                .build();

        return mapToResponse(commentRepository.save(comment));
    }

    public List<CommentResponse> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtDesc(postId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleCommentLike(Long commentId) {
        User user = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        commentLikeRepository.findByCommentIdAndUserId(commentId, user.getId())
                .ifPresentOrElse(
                        commentLikeRepository::delete,
                        () -> commentLikeRepository.save(CommentLike.builder().comment(comment).user(user).build())
                );
    }

    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        User currentUser = getCurrentUser();
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse mapToResponse(Comment comment) {
        User user = comment.getUser();
        UserResponse userDto = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .build();

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean liked = comment.getLikes().stream()
                .anyMatch(like -> like.getUser().getEmail().equals(currentUserEmail));

        List<CommentResponse> replies = comment.getReplies().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(userDto)
                .createdAt(comment.getCreatedAt())
                .likesCount((long) comment.getLikes().size())
                .likedByCurrentUser(liked)
                .replies(replies)
                .build();
    }
}
