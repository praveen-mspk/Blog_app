package com.bloghub.controller;

import com.bloghub.dto.ActivityLogRequest;
import com.bloghub.dto.ReaderDashboardResponse;
import com.bloghub.dto.WriterDashboardResponse;
import com.bloghub.entity.User;
import com.bloghub.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.bloghub.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/read")
    public ResponseEntity<Void> logRead(@RequestBody ActivityLogRequest request) {
        User user = getCurrentUser();
        activityService.logReadingActivity(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/write")
    public ResponseEntity<Void> logWrite(@RequestBody ActivityLogRequest request) {
        User user = getCurrentUser();
        activityService.logWritingActivity(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard/reader")
    public ResponseEntity<?> getReaderDashboard() {
        try {
            User user = getCurrentUser();
            return ResponseEntity.ok(activityService.getReaderDashboard(user.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Reader Dashboard Error: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard/writer")
    public ResponseEntity<?> getWriterDashboard() {
        try {
            User user = getCurrentUser();
            return ResponseEntity.ok(activityService.getWriterDashboard(user.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Writer Dashboard Error: " + e.getMessage() + "\n" + java.util.Arrays.toString(e.getStackTrace()));
        }
    }

    // Temporary diagnostic endpoint
    @GetMapping("/api/v1/auth/test-activity")
    public ResponseEntity<?> testActivity() {
        try {
            return ResponseEntity.ok(activityService.getWriterDashboard(1L)); // Assuming author ID 1
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            return ResponseEntity.internalServerError().body(sw.toString());
        }
    }
}