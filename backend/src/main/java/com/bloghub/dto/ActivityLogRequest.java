package com.bloghub.dto;

import lombok.Data;

@Data
public class ActivityLogRequest {
    private Long postId;
    private Integer value; // durationMinutes for reading, or wordsWritten for writing
}
