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
public class ReaderDashboardResponse {
    private int currentStreak;
    private int totalReadingDays;
    private long totalArticlesRead;
    private int totalReadingMinutes;
    private Map<String, Integer> heatmapData; 
    private Set<String> achievements;
}