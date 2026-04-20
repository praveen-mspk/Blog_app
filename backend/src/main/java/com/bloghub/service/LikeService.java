package com.bloghub.service;

import com.bloghub.entity.Post;
import com.bloghub.entity.PostLike;
import com.bloghub.entity.User;
import com.bloghub.repository.PostLikeRepository;
import com.bloghub.repository.PostRepository;
import com.bloghub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void togglePostLike(Long postId) {
        User user = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        postLikeRepository.findByPostIdAndUserId(postId, user.getId())
                .ifPresentOrElse(
                        postLikeRepository::delete,
                        () -> postLikeRepository.save(PostLike.builder().post(post).user(user).build())
                );
    }
}
