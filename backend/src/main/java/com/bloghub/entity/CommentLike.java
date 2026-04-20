package com.bloghub.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comment_likes", uniqueConstraints = {@UniqueConstraint(columnNames = {"comment_id", "user_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}