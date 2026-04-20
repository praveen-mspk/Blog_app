package com.bloghub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "entity_id", "entity_type", "action_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum EntityType {
        DISCUSSION, REPLY
    }

    public enum ActionType {
        UPVOTE, DOWNVOTE, BOOKMARK, REPORT, SUBSCRIBE
    }
}