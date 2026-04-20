package com.bloghub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementType achievementType;

    @CreationTimestamp
    private LocalDateTime earnedAt;

    public enum AchievementType {
        // Reader Achievements
        FIRST_READ,
        SEVEN_DAY_STREAK,
        THIRTY_DAY_STREAK,
        EXPLORER, // Read 5 different categories
        BOOKWORM, // Read 100 articles
        NIGHT_OWL, // Read 10 articles after midnight
        WEEKEND_WARRIOR, // Read on 10 weekends

        // Writer Achievements
        FIRST_DRAFT,
        CONSISTENT_WRITER, // Write for 14 config days
        PROLIFIC, // Publish 50 articles
        WORD_MASTER, // 100k words
        EDITORS_CHOICE, // 100 likes
        NIGHT_WRITER, // Write after midnight
        WEEKEND_CREATOR
    }
}