package com.trading.reconciliation.repository;

import com.trading.reconciliation.model.ReconciliationStatus;
import com.trading.reconciliation.model.TradeReconciliation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TradeReconciliation entity
 */
@Repository
public interface TradeReconciliationRepository extends JpaRepository<TradeReconciliation, Long> {
    
    /**
     * Find reconciliation by tradeId
     * @param tradeId The trade identifier
     * @return An Optional containing the reconciliation if found
     */
    Optional<TradeReconciliation> findByTradeId(String tradeId);
    
    /**
     * Find reconciliations by status
     * @param status The reconciliation status
     * @param pageable Pagination information
     * @return Page of reconciliations with the specified status
     */
    Page<TradeReconciliation> findByStatus(ReconciliationStatus status, Pageable pageable);
    
    /**
     * Find reconciliations by status
     * @param status The reconciliation status
     * @return List of reconciliations with the specified status
     */
    List<TradeReconciliation> findByStatus(ReconciliationStatus status);
    
    /**
     * Find pending reconciliations that have timed out
     * @param status The PENDING status
     * @param timeoutThreshold The time threshold for timeout
     * @return List of pending reconciliations that have timed out
     */
    @Query("SELECT r FROM TradeReconciliation r WHERE r.status = :status AND r.createdAt < :timeoutThreshold")
    List<TradeReconciliation> findTimedOutReconciliations(
            @Param("status") ReconciliationStatus status,
            @Param("timeoutThreshold") LocalDateTime timeoutThreshold);
} 