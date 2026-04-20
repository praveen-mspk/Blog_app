package com.bloghub.controller;

import com.bloghub.entity.PageView;
import com.bloghub.entity.Post;
import com.bloghub.entity.User;
import com.bloghub.repository.PageViewRepository;
import com.bloghub.repository.PostRepository;
import com.bloghub.repository.UserRepository;
import com.bloghub.service.FunnelAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FunnelAnalyticsController {

    private final FunnelAnalyticsService funnelAnalyticsService;
    private final PageViewRepository pageViewRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // ─── Record a page view ───
    @PostMapping("/pageviews")
    public ResponseEntity<Long> recordPageView(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long postId = Long.valueOf(body.get("postId").toString());
        Post post = postRepository.findById(postId).orElseThrow();

        User user = null;
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            user = userRepository.findByEmail(email).orElse(null);
        } catch (Exception ignored) {}

        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();

        PageView pv = PageView.builder()
                .post(post)
                .user(user)
                .ipAddress(ip)
                .country(body.getOrDefault("country", "Unknown").toString())
                .city(body.getOrDefault("city", "Unknown").toString())
                .deviceType(parseDeviceType(userAgent))
                .os(parseOs(userAgent))
                .browser(parseBrowser(userAgent))
                .referrer(body.getOrDefault("referrer", "").toString())
                .readDurationSeconds(0)
                .scrollDepth(0)
                .build();

        pv = pageViewRepository.save(pv);
        return ResponseEntity.ok(pv.getId());
    }

    // ─── Update page view with duration/scroll on unmount ───
    @RequestMapping(value = "/pageviews/{id}", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<Void> updatePageView(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PageView pv = pageViewRepository.findById(id).orElseThrow();
        if (body.containsKey("readDurationSeconds")) {
            pv.setReadDurationSeconds(Integer.parseInt(body.get("readDurationSeconds").toString()));
        }
        if (body.containsKey("scrollDepth")) {
            pv.setScrollDepth(Integer.parseInt(body.get("scrollDepth").toString()));
        }
        pageViewRepository.save(pv);
        return ResponseEntity.ok().build();
    }

    // ─── Analytics Endpoints ───
    @GetMapping("/analytics/funnel/demographics")
    public ResponseEntity<Map<String, Object>> getDemographics() {
        return ResponseEntity.ok(funnelAnalyticsService.getDemographics());
    }

    @GetMapping("/analytics/funnel/devices")
    public ResponseEntity<Map<String, Object>> getDevices() {
        return ResponseEntity.ok(funnelAnalyticsService.getDeviceMatrix());
    }

    @GetMapping("/analytics/funnel/journey")
    public ResponseEntity<Map<String, Object>> getJourney() {
        return ResponseEntity.ok(funnelAnalyticsService.getReaderFunnel());
    }

    @GetMapping("/analytics/funnel/time-patterns")
    public ResponseEntity<Map<String, Object>> getTimePatterns() {
        return ResponseEntity.ok(funnelAnalyticsService.getTimePatterns());
    }

    @GetMapping("/analytics/funnel/retention")
    public ResponseEntity<Map<String, Object>> getRetention() {
        return ResponseEntity.ok(funnelAnalyticsService.getRetention());
    }

    @GetMapping("/analytics/funnel/recent-activity")
    public ResponseEntity<java.util.List<Map<String, Object>>> getRecentActivity() {
        return ResponseEntity.ok(funnelAnalyticsService.getRecentActivity());
    }

    // ─── Simple UA parsers ───
    private PageView.DeviceType parseDeviceType(String ua) {
        if (ua == null) return PageView.DeviceType.DESKTOP;
        ua = ua.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android")) return PageView.DeviceType.MOBILE;
        if (ua.contains("tablet") || ua.contains("ipad")) return PageView.DeviceType.TABLET;
        return PageView.DeviceType.DESKTOP;
    }

    private String parseOs(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS")) return "macOS";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("Linux")) return "Linux";
        return "Other";
    }

    private String parseBrowser(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Edg")) return "Edge";
        if (ua.contains("Chrome") && !ua.contains("Edg")) return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari") && !ua.contains("Chrome")) return "Safari";
        return "Other";
    }
}