package com.bloghub.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DiscussionResponse {
    private Long id;
    private Long communityId;
    private UserResponse author;
    private String title;
    private String content;
    private String type;
    private String status;
    private long views;
    private long repliesCount;
    private long upvotes;
    private long downvotes;
    private String userVote; // UPVOTE or DOWNVOTE or null
    private LocalDateTime createdAt;
}