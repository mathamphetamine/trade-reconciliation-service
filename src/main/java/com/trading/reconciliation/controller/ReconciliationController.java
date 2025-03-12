package com.trading.reconciliation.controller;

import com.trading.reconciliation.dto.ReconciliationResponseDto;
import com.trading.reconciliation.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for reconciliation status operations
 * 
 * This controller provides endpoints for retrieving reconciliation status information
 * and triggering reconciliation processes manually. It allows clients to:
 * 
 * 1. Get the reconciliation status for a specific trade
 * 2. Get a paginated list of reconciliations with optional filtering
 * 3. Manually trigger a reconciliation process for a trade
 * 
 * The controller uses Spring's ResponseEntity to provide appropriate HTTP status codes
 * and response bodies. It also supports standard pagination and sorting for list queries.
 */
@RestController
@RequestMapping("/reconciliations")
@Slf4j
@RequiredArgsConstructor
public class ReconciliationController {
    
    private final ReconciliationService reconciliationService;
    
    /**
     * Get reconciliation status for a specific trade
     * 
     * This endpoint retrieves the current reconciliation status for a specific trade.
     * If the trade ID is found, it returns the reconciliation details with HTTP 200.
     * If the trade ID is not found, it returns HTTP 404.
     * 
     * Example response:
     * {
     *   "tradeId": "T123456",
     *   "status": "MATCHED",
     *   "statusDescription": "Matched",
     *   "details": "Trades matched successfully",
     *   "createdAt": "2023-06-15T10:35:00",
     *   "updatedAt": "2023-06-15T10:35:05",
     *   "lastReconciliationAttempt": "2023-06-15T10:35:05"
     * }
     * 
     * @param tradeId The trade identifier
     * @return Reconciliation status response
     */
    @GetMapping("/{tradeId}")
    public ResponseEntity<ReconciliationResponseDto> getReconciliationStatus(@PathVariable String tradeId) {
        log.info("Getting reconciliation status for tradeId: {}", tradeId);
        
        return reconciliationService.getReconciliationStatus(tradeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get paginated list of reconciliations with optional status filter
     * 
     * This endpoint retrieves a paginated list of reconciliation statuses.
     * It supports filtering by status, pagination, and sorting by updated time.
     * 
     * Example URL: /reconciliations?status=MISMATCHED&page=0&size=20
     * 
     * @param status Optional status filter (PENDING, MATCHED, MISMATCHED, RECONCILIATION_TIMEOUT, ERROR)
     * @param page Page number (0-based, default: 0)
     * @param size Page size (default: 20)
     * @return Page of reconciliation responses
     */
    @GetMapping
    public ResponseEntity<Page<ReconciliationResponseDto>> getReconciliations(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting reconciliations with status: {}, page: {}, size: {}", status, page, size);
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<ReconciliationResponseDto> reconciliations = reconciliationService.getReconciliations(status, pageRequest);
        
        return ResponseEntity.ok(reconciliations);
    }
    
    /**
     * Manually trigger reconciliation for a trade
     * 
     * This endpoint allows manual triggering of the reconciliation process for a specific trade.
     * It's useful for re-running reconciliation after data changes or for testing purposes.
     * 
     * The reconciliation happens asynchronously, so the endpoint returns HTTP 202 (Accepted)
     * to indicate that the request has been accepted but processing may not be complete.
     * 
     * @param tradeId The trade identifier
     * @return HTTP 202 Accepted response
     */
    @PostMapping("/{tradeId}/trigger")
    public ResponseEntity<Void> triggerReconciliation(@PathVariable String tradeId) {
        log.info("Manually triggering reconciliation for tradeId: {}", tradeId);
        
        try {
            reconciliationService.triggerReconciliation(tradeId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (Exception e) {
            log.error("Error triggering reconciliation for tradeId: {}", tradeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 