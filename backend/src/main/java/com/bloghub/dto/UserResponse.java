package com.bloghub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String bio;
    private String profileImage;
    
    // Subscription fields
    private boolean isMember;

    @com.fasterxml.jackson.annotation.JsonProperty("isMember")
    public boolean isMember() {
        return isMember;
    }

    private String subscriptionType;
    private Integer freeStoriesRemaining;
}
