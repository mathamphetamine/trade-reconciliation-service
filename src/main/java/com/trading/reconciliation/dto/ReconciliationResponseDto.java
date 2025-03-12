package com.trading.reconciliation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trading.reconciliation.model.ReconciliationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for reconciliation results to be returned via API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationResponseDto {
    
    private String tradeId;
    private ReconciliationStatus status;
    private String statusDescription;
    private String details;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastReconciliationAttempt;
} 