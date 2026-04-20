package com.bloghub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "page_views")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // null for anonymous views

    private String ipAddress;
    private String country;
    private String city;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeviceType deviceType = DeviceType.DESKTOP;

    private String os;
    private String browser;

    @Builder.Default
    private Integer readDurationSeconds = 0;

    @Builder.Default
    private Integer scrollDepth = 0; // 0-100%

    private String referrer;

    @CreationTimestamp
    private LocalDateTime viewedAt;

    public enum DeviceType {
        DESKTOP, MOBILE, TABLET
    }
}
