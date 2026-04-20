package com.bloghub.service;

import com.bloghub.dto.CommunityRequest;
import com.bloghub.dto.CommunityResponse;
import com.bloghub.dto.UserResponse;
import com.bloghub.entity.*;
import com.bloghub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final CommunityInviteRepository inviteRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public CommunityResponse createCommunity(CommunityRequest request) {
        User creator = getCurrentUser();

        Community community = Community.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(Community.Type.valueOf(request.getType().toUpperCase()))
                .creator(creator)
                .build();

        Community saved = communityRepository.save(community);

        CommunityMember member = CommunityMember.builder()
                .community(saved)
                .user(creator)
                .role(CommunityMember.Role.CREATOR)
                .build();
        memberRepository.save(member);

        return mapToResponse(saved, 1L);
    }

    public List<CommunityResponse> getAllCommunities() {
        return communityRepository.findAll().stream()
                .map(c -> {
                    long memberCount = memberRepository.findByCommunityId(c.getId()).size();
                    return mapToResponse(c, memberCount);
                })
                .collect(Collectors.toList());
    }

    public CommunityResponse getCommunityById(Long id) {
        Community community = communityRepository.findById(id).orElseThrow();
        long memberCount = memberRepository.findByCommunityId(id).size();
        CommunityResponse resp = mapToResponse(community, memberCount);

        // Attach membership status for current user
        try {
            User user = getCurrentUser();
            boolean isMember = memberRepository.existsByCommunityIdAndUserId(id, user.getId());
            resp.setIsMember(isMember);
            boolean isCreator = community.getCreator().getId().equals(user.getId());
            resp.setIsCreator(isCreator);

            if (!isMember && community.getType() == Community.Type.MODERATED) {
                Optional<JoinRequest> jr = joinRequestRepository.findByCommunityIdAndUserId(id, user.getId());
                jr.ifPresent(r -> resp.setPendingRequest(r.getStatus() == JoinRequest.Status.PENDING));
            }
        } catch (Exception ignored) {
            // Not logged in
        }

        return resp;
    }

    // ─── OPEN: direct join ───
    @Transactional
    public void joinCommunity(Long communityId) {
        User user = getCurrentUser();
        Community community = communityRepository.findById(communityId).orElseThrow();

        if (memberRepository.existsByCommunityIdAndUserId(communityId, user.getId())) {
            throw new RuntimeException("Already a member");
        }

        if (community.getType() == Community.Type.MODERATED) {
            throw new RuntimeException("This community requires approval. Use the request-to-join endpoint.");
        }
        if (community.getType() == Community.Type.PRIVATE) {
            throw new RuntimeException("This community is invite-only. Use an invite link.");
        }

        CommunityMember member = CommunityMember.builder()
                .community(community)
                .user(user)
                .role(CommunityMember.Role.MEMBER)
                .build();
        memberRepository.save(member);
    }

    // ─── MODERATED: request to join ───
    @Transactional
    public void requestToJoin(Long communityId) {
        User user = getCurrentUser();
        Community community = communityRepository.findById(communityId).orElseThrow();

        if (community.getType() != Community.Type.MODERATED) {
            throw new RuntimeException("This community does not require join requests.");
        }
        if (memberRepository.existsByCommunityIdAndUserId(communityId, user.getId())) {
            throw new RuntimeException("Already a member");
        }
        if (joinRequestRepository.existsByCommunityIdAndUserId(communityId, user.getId())) {
            throw new RuntimeException("You have already sent a request");
        }

        JoinRequest request = JoinRequest.builder()
                .community(community)
                .user(user)
                .build();
        joinRequestRepository.save(request);
    }

    public List<Map<String, Object>> getPendingRequests(Long communityId) {
        User user = getCurrentUser();
        Community community = communityRepository.findById(communityId).orElseThrow();
        if (!community.getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("Only the creator can view pending requests");
        }

        return joinRequestRepository.findByCommunityIdAndStatus(communityId, JoinRequest.Status.PENDING)
                .stream().map(jr -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("requestId", jr.getId());
                    m.put("userId", jr.getUser().getId());
                    m.put("userName", jr.getUser().getName());
                    m.put("userEmail", jr.getUser().getEmail());
                    m.put("requestedAt", jr.getRequestedAt().toString());
                    return m;
                }).collect(Collectors.toList());
    }

    @Transactional
    public void approveRequest(Long requestId) {
        User user = getCurrentUser();
        JoinRequest jr = joinRequestRepository.findById(requestId).orElseThrow();
        Community community = jr.getCommunity();

        if (!community.getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("Only the creator can approve requests");
        }

        jr.setStatus(JoinRequest.Status.APPROVED);
        joinRequestRepository.save(jr);

        CommunityMember member = CommunityMember.builder()
                .community(community)
                .user(jr.getUser())
                .role(CommunityMember.Role.MEMBER)
                .build();
        memberRepository.save(member);
    }

    @Transactional
    public void rejectRequest(Long requestId) {
        User user = getCurrentUser();
        JoinRequest jr = joinRequestRepository.findById(requestId).orElseThrow();
        if (!jr.getCommunity().getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("Only the creator can reject requests");
        }
        jr.setStatus(JoinRequest.Status.REJECTED);
        joinRequestRepository.save(jr);
    }

    // ─── PRIVATE: invite link flow ───
    @Transactional
    public String generateInviteLink(Long communityId) {
        User user = getCurrentUser();
        Community community = communityRepository.findById(communityId).orElseThrow();

        if (!community.getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("Only the creator can generate invite links");
        }
        if (community.getType() != Community.Type.PRIVATE) {
            throw new RuntimeException("Invite links are only for private communities");
        }

        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        CommunityInvite invite = CommunityInvite.builder()
                .community(community)
                .inviteCode(code)
                .createdBy(user)
                .build();
        inviteRepository.save(invite);
        return code;
    }

    @Transactional
    public CommunityResponse joinViaInvite(String inviteCode) {
        User user = getCurrentUser();
        CommunityInvite invite = inviteRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        Community community = invite.getCommunity();

        if (memberRepository.existsByCommunityIdAndUserId(community.getId(), user.getId())) {
            throw new RuntimeException("Already a member");
        }

        CommunityMember member = CommunityMember.builder()
                .community(community)
                .user(user)
                .role(CommunityMember.Role.MEMBER)
                .build();
        memberRepository.save(member);

        long memberCount = memberRepository.findByCommunityId(community.getId()).size();
        return mapToResponse(community, memberCount);
    }

    private CommunityResponse mapToResponse(Community community, long memberCount) {
        return CommunityResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
                .type(community.getType().name())
                .memberCount(memberCount)
                .creator(UserResponse.builder()
                        .id(community.getCreator().getId())
                        .name(community.getCreator().getName())
                        .email(community.getCreator().getEmail())
                        .build())
                .createdAt(community.getCreatedAt())
                .build();
    }

    // ─── Member Management ───
    public List<Map<String, Object>> getMembers(Long communityId) {
        return memberRepository.findByCommunityId(communityId).stream()
                .map(m -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("memberId", m.getId());
                    info.put("userId", m.getUser().getId());
                    info.put("userName", m.getUser().getName());
                    info.put("userEmail", m.getUser().getEmail());
                    info.put("role", m.getRole().name());
                    info.put("joinedAt", m.getJoinedAt().toString());
                    return info;
                }).collect(Collectors.toList());
    }

    @Transactional
    public void removeMember(Long communityId, Long memberId) {
        User user = getCurrentUser();
        Community community = communityRepository.findById(communityId).orElseThrow();

        if (!community.getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("Only the creator can remove members");
        }

        CommunityMember member = memberRepository.findById(memberId).orElseThrow();

        if (member.getRole() == CommunityMember.Role.CREATOR) {
            throw new RuntimeException("Cannot remove the community creator");
        }

        memberRepository.delete(member);
    }
}
