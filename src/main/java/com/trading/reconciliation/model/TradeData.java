package com.trading.reconciliation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity class representing raw trade data received from source systems
 */
@Entity
@Table(name = "trade_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id", nullable = false)
    private String tradeId;

    @Column(name = "instrument", nullable = false)
    private String instrument;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "trade_date", nullable = false)
    private LocalDateTime tradeDate;

    @Column(name = "counterparty", nullable = false)
    private String counterparty;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;
} 