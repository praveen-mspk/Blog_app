package com.bloghub.service;

import com.bloghub.dto.*;
import com.bloghub.entity.*;
import com.bloghub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MainCategoryRepository mainCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final AccessControlService accessControlService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public PostResponse createPost(PostRequest request) {
        User author = getCurrentUser();
        MainCategory mainCategory = mainCategoryRepository.findById(request.getMainCategoryId())
                .orElseThrow(() -> new RuntimeException("Main category not found"));

        List<SubCategory> subCategories = subCategoryRepository.findAllById(request.getSubCategoryIds());

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .featuredImage(request.getFeaturedImage())
                .author(author)
                .mainCategory(mainCategory)
                .subCategories(new HashSet<>(subCategories))
                .status(Post.Status.valueOf(request.getStatus().toUpperCase()))
                .viewsCount(0L)
                .build();

        Post savedPost = postRepository.save(post);
        return mapToResponse(savedPost);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Long categoryId, Integer month, Integer year, Pageable pageable) {
        Specification<Post> spec = PostSpecification.filterPosts(categoryId, month, year, "PUBLISHED");
        return postRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String query, Pageable pageable) {
        return postRepository.searchPosts(query, pageable)
                .map(this::mapToResponse);
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        User currentUser = null;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
        }

        // Enforce paywall and decrement meter if necessary
        if (post.isPremium()) {
            accessControlService.canAccessPost(currentUser, post);
        }

        // Increment views
        post.setViewsCount(post.getViewsCount() + 1);
        postRepository.save(post);
        
        return mapToResponse(post);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User currentUser = getCurrentUser();
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to update this post");
        }

        MainCategory mainCategory = mainCategoryRepository.findById(request.getMainCategoryId())
                .orElseThrow(() -> new RuntimeException("Main category not found"));

        List<SubCategory> subCategories = subCategoryRepository.findAllById(request.getSubCategoryIds());

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setFeaturedImage(request.getFeaturedImage());
        post.setMainCategory(mainCategory);
        post.setSubCategories(new HashSet<>(subCategories));
        post.setStatus(Post.Status.valueOf(request.getStatus().toUpperCase()));

        Post updatedPost = postRepository.save(post);
        return mapToResponse(updatedPost);
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User currentUser = getCurrentUser();
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to delete this post");
        }

        postRepository.delete(post);
    }

    public List<PostResponse> getUserPosts() {
        User user = getCurrentUser();
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PostResponse mapToResponse(Post post) {
        User author = post.getAuthor();
        UserResponse authorDto = UserResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .email(author.getEmail())
                .bio(author.getBio())
                .profileImage(author.getProfileImage())
                .build();

        CategoryResponse mainCategoryDto = CategoryResponse.builder()
                .id(post.getMainCategory().getId())
                .name(post.getMainCategory().getName())
                .build();

        List<CategoryResponse> subCategoryDtos = post.getSubCategories().stream()
                .map(sub -> CategoryResponse.builder()
                        .id(sub.getId())
                        .name(sub.getName())
                        .build())
                .collect(Collectors.toList());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean liked = false;
        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated() && ! (authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            currentUser = userRepository.findByEmail(email).orElse(null);
            if (currentUser != null) {
                liked = post.getLikes().stream()
                    .anyMatch(like -> like.getUser().getEmail().equals(email));
            }
        }

        boolean isLocked = false;
        String content = post.getContent();

        // Access Control Logic
        if (post.isPremium()) {
            if (currentUser == null || !currentUser.isMember()) {
                // Not a member - check if we should lock
                // For list view (getAllPosts/search), we can keep it locked or just show a partial
                // For detailed view, the Controller/Service should handle decrementing the meter
                // Here we just mark it based on current state
                if (currentUser == null || currentUser.getFreeStoriesRemaining() <= 0) {
                   isLocked = true;
                   content = content.length() > 300 ? content.substring(0, 300) + "..." : content;
                }
            }
        }

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(content)
                .featuredImage(post.getFeaturedImage())
                .author(authorDto)
                .mainCategory(mainCategoryDto)
                .subCategories(subCategoryDtos)
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .viewsCount(post.getViewsCount())
                .likesCount((long) post.getLikes().size())
                .commentsCount((long) post.getComments().size())
                .likedByCurrentUser(liked)
                .isPremium(post.isPremium())
                .isLocked(isLocked)
                .build();
    }
}