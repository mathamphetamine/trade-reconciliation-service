package com.trading.reconciliation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for trade data to be received via API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeDataDto {
    
    private String tradeId;
    private String instrument;
    private BigDecimal quantity;
    private BigDecimal price;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime tradeDate;
    
    private String counterparty;
} 