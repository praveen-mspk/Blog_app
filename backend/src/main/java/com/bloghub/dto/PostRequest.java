package com.bloghub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {
    private String title;
    private String content;
    private String featuredImage;
    private Long mainCategoryId;
    private List<Long> subCategoryIds;
    private String status; // DRAFT, PUBLISHED
}
