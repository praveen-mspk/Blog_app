package com.bloghub.controller;

import com.bloghub.dto.CommunityRequest;
import com.bloghub.dto.CommunityResponse;
import com.bloghub.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    public ResponseEntity<CommunityResponse> createCommunity(@RequestBody CommunityRequest request) {
        return ResponseEntity.ok(communityService.createCommunity(request));
    }

    @GetMapping
    public ResponseEntity<List<CommunityResponse>> getAllCommunities() {
        return ResponseEntity.ok(communityService.getAllCommunities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityResponse> getCommunityById(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.getCommunityById(id));
    }

    // ─── OPEN: direct join ───
    @PostMapping("/{id}/join")
    public ResponseEntity<Void> joinCommunity(@PathVariable Long id) {
        communityService.joinCommunity(id);
        return ResponseEntity.ok().build();
    }

    // ─── MODERATED: request to join ───
    @PostMapping("/{id}/request-join")
    public ResponseEntity<Void> requestToJoin(@PathVariable Long id) {
        communityService.requestToJoin(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/pending-requests")
    public ResponseEntity<List<Map<String, Object>>> getPendingRequests(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.getPendingRequests(id));
    }

    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<Void> approveRequest(@PathVariable Long requestId) {
        communityService.approveRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long requestId) {
        communityService.rejectRequest(requestId);
        return ResponseEntity.ok().build();
    }

    // ─── PRIVATE: invite link flow ───
    @PostMapping("/{id}/generate-invite")
    public ResponseEntity<Map<String, String>> generateInviteLink(@PathVariable Long id) {
        String code = communityService.generateInviteLink(id);
        return ResponseEntity.ok(Map.of("inviteCode", code));
    }

    @PostMapping("/join-via-invite/{code}")
    public ResponseEntity<CommunityResponse> joinViaInvite(@PathVariable String code) {
        return ResponseEntity.ok(communityService.joinViaInvite(code));
    }

    // ─── Member Management ───
    @GetMapping("/{id}/members")
    public ResponseEntity<List<Map<String, Object>>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.getMembers(id));
    }

    @DeleteMapping("/{communityId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long communityId, @PathVariable Long memberId) {
        communityService.removeMember(communityId, memberId);
        return ResponseEntity.ok().build();
    }
}
