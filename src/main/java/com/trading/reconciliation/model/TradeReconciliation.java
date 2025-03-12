package com.trading.reconciliation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing trade reconciliation results
 */
@Entity
@Table(name = "trade_reconciliation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeReconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id", nullable = false, unique = true)
    private String tradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReconciliationStatus status;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "system_a_trade_id")
    private Long systemATradeId;

    @Column(name = "system_b_trade_id")
    private Long systemBTradeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_reconciliation_attempt")
    private LocalDateTime lastReconciliationAttempt;

    /**
     * Pre-persist hook to set createdAt and updatedAt fields
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Pre-update hook to set updatedAt field
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 