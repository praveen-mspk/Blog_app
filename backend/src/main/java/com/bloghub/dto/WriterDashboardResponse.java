package com.bloghub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriterDashboardResponse {
    private int currentStreak;
    private int totalWritingDays;
    private long totalArticlesPublished;
    private long totalWordsWritten;
    private Map<String, Integer> heatmapData; 
    private Set<String> achievements;
}