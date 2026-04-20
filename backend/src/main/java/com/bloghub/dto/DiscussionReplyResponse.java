package com.bloghub.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DiscussionReplyResponse {
    private Long id;
    private Long discussionId;
    private UserResponse author;
    private String content;
    private boolean isBestAnswer;
    private long upvotes;
    private long downvotes;
    private String userVote;
    private LocalDateTime createdAt;
}