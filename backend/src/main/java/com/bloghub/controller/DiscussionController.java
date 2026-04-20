package com.bloghub.controller;

import com.bloghub.dto.*;
import com.bloghub.service.DiscussionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discussions")
@RequiredArgsConstructor
public class DiscussionController {
    private final DiscussionService discussionService;

    @PostMapping("/community/{communityId}")
    public ResponseEntity<DiscussionResponse> createDiscussion(@PathVariable Long communityId, @RequestBody DiscussionRequest request) {
        return ResponseEntity.ok(discussionService.createDiscussion(communityId, request));
    }

    @GetMapping("/community/{communityId}")
    public ResponseEntity<List<DiscussionResponse>> getCommunityDiscussions(@PathVariable Long communityId) {
        return ResponseEntity.ok(discussionService.getCommunityDiscussions(communityId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscussionResponse> getDiscussionById(@PathVariable Long id) {
        return ResponseEntity.ok(discussionService.getDiscussionById(id));
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<DiscussionReplyResponse> addReply(@PathVariable Long id, @RequestBody DiscussionReplyRequest request) {
        return ResponseEntity.ok(discussionService.addReply(id, request));
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<List<DiscussionReplyResponse>> getReplies(@PathVariable Long id) {
        return ResponseEntity.ok(discussionService.getReplies(id));
    }
}