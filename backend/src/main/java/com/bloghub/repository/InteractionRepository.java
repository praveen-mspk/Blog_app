package com.bloghub.repository;

import com.bloghub.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    Optional<Interaction> findByUserIdAndEntityIdAndEntityTypeAndActionType(
        Long userId, Long entityId, Interaction.EntityType entityType, Interaction.ActionType actionType
    );
    long countByEntityIdAndEntityTypeAndActionType(
        Long entityId, Interaction.EntityType entityType, Interaction.ActionType actionType
    );
}