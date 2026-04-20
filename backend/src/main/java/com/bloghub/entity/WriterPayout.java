package com.bloghub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "writer_payouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WriterPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    private BigDecimal amount;
    
    private LocalDateTime payoutDate;

    @Enumerated(EnumType.STRING)
    private PayoutStatus status;

    public enum PayoutStatus {
        PENDING, PAID, FAILED
    }
}