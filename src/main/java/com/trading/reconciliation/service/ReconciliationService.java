package com.trading.reconciliation.service;

import com.trading.reconciliation.dto.ReconciliationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service interface for trade reconciliation operations
 */
public interface ReconciliationService {
    
    /**
     * Trigger reconciliation for a trade
     * @param tradeId The trade identifier
     */
    void triggerReconciliation(String tradeId);
    
    /**
     * Execute reconciliation for a trade
     * @param tradeId The trade identifier
     */
    void executeReconciliation(String tradeId);
    
    /**
     * Process timeout for pending reconciliations
     */
    void processTimeouts();
    
    /**
     * Get reconciliation status for a trade
     * @param tradeId The trade identifier
     * @return Optional containing the reconciliation response if found
     */
    Optional<ReconciliationResponseDto> getReconciliationStatus(String tradeId);
    
    /**
     * Get paginated list of reconciliations with optional status filter
     * @param status Optional status filter
     * @param pageable Pagination information
     * @return Page of reconciliation responses
     */
    Page<ReconciliationResponseDto> getReconciliations(String status, Pageable pageable);
} 