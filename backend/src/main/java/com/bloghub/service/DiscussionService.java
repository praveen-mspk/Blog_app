package com.bloghub.service;

import com.bloghub.dto.*;
import com.bloghub.entity.*;
import com.bloghub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscussionService {
    private final DiscussionRepository discussionRepository;
    private final DiscussionReplyRepository replyRepository;
    private final CommunityRepository communityRepository;
    private final InteractionRepository interactionRepository;
    private final UserRepository userRepository;
    private final RealtimeService realtimeService;

    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    @Transactional
    public DiscussionResponse createDiscussion(Long communityId, DiscussionRequest request) {
        User author = getCurrentUser();
        if (author == null) throw new RuntimeException("Must be logged in.");
        
        Community community = communityRepository.findById(communityId).orElseThrow();
        
        Discussion discussion = Discussion.builder()
                .community(community)
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .type(Discussion.Type.valueOf(request.getType().toUpperCase()))
                .build();
                
        Discussion saved = discussionRepository.save(discussion);
        DiscussionResponse response = mapToDiscussionResponse(saved);
        
        // Broadcast to all users viewing this community
        realtimeService.broadcastToCommunity(communityId, "NEW_DISCUSSION", response);
        
        return response;
    }

    public List<DiscussionResponse> getCommunityDiscussions(Long communityId) {
        return discussionRepository.findByCommunityIdOrderByCreatedAtDesc(communityId).stream()
                .map(this::mapToDiscussionResponse)
                .collect(Collectors.toList());
    }

    public DiscussionResponse getDiscussionById(Long id) {
        Discussion discussion = discussionRepository.findById(id).orElseThrow();
        discussion.setViews(discussion.getViews() + 1);
        discussionRepository.save(discussion);
        return mapToDiscussionResponse(discussion);
    }
    
    @Transactional
    public DiscussionReplyResponse addReply(Long discussionId, DiscussionReplyRequest request) {
        User author = getCurrentUser();
        if (author == null) throw new RuntimeException("Must be logged in.");
        
        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow();
        
        DiscussionReply reply = DiscussionReply.builder()
                .discussion(discussion)
                .author(author)
                .content(request.getContent())
                .build();
                
        DiscussionReply saved = replyRepository.save(reply);
        DiscussionReplyResponse response = mapToReplyResponse(saved);
        
        // Broadcast to all users viewing this discussion thread
        realtimeService.broadcastToDiscussion(discussionId, "NEW_REPLY", response);
        
        return response;
    }
    
    public List<DiscussionReplyResponse> getReplies(Long discussionId) {
        return replyRepository.findByDiscussionIdOrderByCreatedAtAsc(discussionId).stream()
                .map(this::mapToReplyResponse)
                .collect(Collectors.toList());
    }

    private DiscussionResponse mapToDiscussionResponse(Discussion d) {
        User currentUser = getCurrentUser();
        long upvotes = interactionRepository.countByEntityIdAndEntityTypeAndActionType(d.getId(), Interaction.EntityType.DISCUSSION, Interaction.ActionType.UPVOTE);
        long downvotes = interactionRepository.countByEntityIdAndEntityTypeAndActionType(d.getId(), Interaction.EntityType.DISCUSSION, Interaction.ActionType.DOWNVOTE);
        
        String userVote = null;
        if (currentUser != null) {
            Optional<Interaction> upvote = interactionRepository.findByUserIdAndEntityIdAndEntityTypeAndActionType(currentUser.getId(), d.getId(), Interaction.EntityType.DISCUSSION, Interaction.ActionType.UPVOTE);
            if (upvote.isPresent()) userVote = "UPVOTE";
            else {
                Optional<Interaction> downvote = interactionRepository.findByUserIdAndEntityIdAndEntityTypeAndActionType(currentUser.getId(), d.getId(), Interaction.EntityType.DISCUSSION, Interaction.ActionType.DOWNVOTE);
                if (downvote.isPresent()) userVote = "DOWNVOTE";
            }
        }

        return DiscussionResponse.builder()
                .id(d.getId())
                .communityId(d.getCommunity().getId())
                .author(UserResponse.builder().id(d.getAuthor().getId()).name(d.getAuthor().getName()).profileImage(d.getAuthor().getProfileImage()).build())
                .title(d.getTitle())
                .content(d.getContent())
                .type(d.getType().name())
                .status(d.getStatus().name())
                .views(d.getViews())
                .createdAt(d.getCreatedAt())
                .repliesCount(d.getReplies().size())
                .upvotes(upvotes)
                .downvotes(downvotes)
                .userVote(userVote)
                .build();
    }
    
    private DiscussionReplyResponse mapToReplyResponse(DiscussionReply r) {
        User currentUser = getCurrentUser();
        long upvotes = interactionRepository.countByEntityIdAndEntityTypeAndActionType(r.getId(), Interaction.EntityType.REPLY, Interaction.ActionType.UPVOTE);
        long downvotes = interactionRepository.countByEntityIdAndEntityTypeAndActionType(r.getId(), Interaction.EntityType.REPLY, Interaction.ActionType.DOWNVOTE);
        
        String userVote = null;
        if (currentUser != null) {
            Optional<Interaction> upvote = interactionRepository.findByUserIdAndEntityIdAndEntityTypeAndActionType(currentUser.getId(), r.getId(), Interaction.EntityType.REPLY, Interaction.ActionType.UPVOTE);
            if (upvote.isPresent()) userVote = "UPVOTE";
            else {
                Optional<Interaction> downvote = interactionRepository.findByUserIdAndEntityIdAndEntityTypeAndActionType(currentUser.getId(), r.getId(), Interaction.EntityType.REPLY, Interaction.ActionType.DOWNVOTE);
                if (downvote.isPresent()) userVote = "DOWNVOTE";
            }
        }

        return DiscussionReplyResponse.builder()
                .id(r.getId())
                .discussionId(r.getDiscussion().getId())
                .author(UserResponse.builder().id(r.getAuthor().getId()).name(r.getAuthor().getName()).profileImage(r.getAuthor().getProfileImage()).build())
                .content(r.getContent())
                .isBestAnswer(r.isBestAnswer())
                .createdAt(r.getCreatedAt())
                .upvotes(upvotes)
                .downvotes(downvotes)
                .userVote(userVote)
                .build();
    }
}