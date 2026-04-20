package com.bloghub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(exclude = {"password", "posts"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String profileImage;

    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Builder.Default
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    // Subscription & Membership
    @Builder.Default
    @Column(name = "is_member")
    private boolean isMember = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionType subscriptionType = SubscriptionType.FREE;

    private LocalDateTime subscriptionEndDate;

    @Builder.Default
    private int freeStoriesRemaining = 3;

    private LocalDateTime lastResetDate;

    public enum SubscriptionType {
        FREE, MONTHLY, ANNUAL
    }
}