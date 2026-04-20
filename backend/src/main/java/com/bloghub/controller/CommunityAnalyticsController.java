package com.bloghub.controller;

import com.bloghub.service.CommunityAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/communities/{id}/analytics")
@RequiredArgsConstructor
public class CommunityAnalyticsController {
    private final CommunityAnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAnalytics(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getAnalytics(id));
    }
}
