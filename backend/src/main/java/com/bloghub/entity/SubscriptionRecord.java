package com.bloghub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true)
    private String stripeSubscriptionId;

    @Column(unique = true)
    private String stripeCustomerId;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private LocalDateTime currentPeriodEnd;

    private boolean cancelAtPeriodEnd;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum SubscriptionStatus {
        ACTIVE, CANCELED, PAST_DUE, TRIALING, INCOMPLETE
    }
}
