package com.bloghub.service;

import com.bloghub.entity.Interaction;
import com.bloghub.entity.User;
import com.bloghub.repository.InteractionRepository;
import com.bloghub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InteractionService {
    private final InteractionRepository interactionRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void recordInteraction(Long entityId, Interaction.EntityType entityType, Interaction.ActionType actionType) {
        User user = getCurrentUser();
        
        // If it's a vote (UPVOTE/DOWNVOTE), we need to clear opposite votes.
        if (actionType == Interaction.ActionType.UPVOTE || actionType == Interaction.ActionType.DOWNVOTE) {
            Interaction.ActionType opposite = (actionType == Interaction.ActionType.UPVOTE) ? Interaction.ActionType.DOWNVOTE : Interaction.ActionType.UPVOTE;
            Optional<Interaction> existingOpposite = interactionRepository.findByUserIdAndEntityIdAndEntityTypeAndActionType(user.getId(), entityId, entityType, opposite);
            existingOpposite.ifPresent(interactionRepository::delete);
        }
        
        Optional<Interaction> existing = interactionRepository.findByUserIdAndEntityIdAndEntityTypeAndActionType(user.getId(), entityId, entityType, actionType);
        
        if (existing.isPresent()) {
            // Toggle off
            interactionRepository.delete(existing.get());
        } else {
            Interaction interaction = Interaction.builder()
                    .user(user)
                    .entityId(entityId)
                    .entityType(entityType)
                    .actionType(actionType)
                    .build();
            interactionRepository.save(interaction);
        }
    }
}