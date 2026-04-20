package com.bloghub.dto;

import lombok.Data;

@Data
public class DiscussionRequest {
    private String title;
    private String content;
    private String type;
}