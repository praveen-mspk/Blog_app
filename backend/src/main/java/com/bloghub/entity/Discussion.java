package com.bloghub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discussions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discussion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.OPEN;

    @Builder.Default
    private long views = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "discussion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscussionReply> replies = new ArrayList<>();

    public enum Type {
        QUESTION, DISCUSSION, ANNOUNCEMENT, POLL, RESOURCE
    }

    public enum Status {
        OPEN, CLOSED
    }
}