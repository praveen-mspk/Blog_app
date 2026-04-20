package com.bloghub.controller;

import com.bloghub.entity.Interaction;
import com.bloghub.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interactions")
@RequiredArgsConstructor
public class InteractionController {
    private final InteractionService interactionService;

    @PostMapping("/{entityType}/{entityId}/{actionType}")
    public ResponseEntity<Void> toggleInteraction(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @PathVariable String actionType) {
        
        interactionService.recordInteraction(
                entityId, 
                Interaction.EntityType.valueOf(entityType.toUpperCase()), 
                Interaction.ActionType.valueOf(actionType.toUpperCase())
        );
        return ResponseEntity.ok().build();
    }
}