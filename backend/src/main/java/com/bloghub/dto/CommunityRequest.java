package com.bloghub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityRequest {
    private String name;
    private String description;
    private String type; // OPEN, MODERATED, PRIVATE, PREMIUM
}
