package com.bloghub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityResponse {
    private Long id;
    private String name;
    private String description;
    private String type;
    private long memberCount;
    private UserResponse creator;
    private LocalDateTime createdAt;
    private Boolean isMember;
    private Boolean isCreator;
    private Boolean pendingRequest;
}
