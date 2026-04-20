package com.bloghub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String featuredImage;
    private UserResponse author;
    private CategoryResponse mainCategory;
    private List<CategoryResponse> subCategories;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long viewsCount;
    private Long likesCount;
    private Long commentsCount;
    private boolean likedByCurrentUser;
    private boolean isPremium;
    private boolean isLocked;
}
