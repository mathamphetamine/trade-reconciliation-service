package com.trading.reconciliation.messaging;

import com.trading.reconciliation.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener for reconciliation task messages from RabbitMQ
 * 
 * This component listens to the reconciliation tasks queue and processes incoming
 * messages to trigger the reconciliation process for the specified trade ID.
 * 
 * The listener uses Spring AMQP's @RabbitListener annotation to automatically
 * register with RabbitMQ and receive messages. The queue name is configured
 * in the application.yml file.
 * 
 * In a production environment, this could be enhanced with:
 * - Dead-letter queue for failed message processing
 * - Retry mechanism for transient failures
 * - Circuit breaker for resilience
 * - Message acknowledge mode configuration
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReconciliationTaskListener {
    
    private final ReconciliationService reconciliationService;
    
    /**
     * Process reconciliation task messages from the reconciliation-tasks queue
     * 
     * This method is called whenever a new message is received on the 
     * reconciliation-tasks queue. It extracts the trade ID from the message
     * and triggers the reconciliation process.
     * 
     * @param tradeId The trade identifier to reconcile
     */
    @RabbitListener(queues = "${reconciliation.queue.reconciliation-tasks}")
    public void processReconciliationTask(String tradeId) {
        log.info("Received reconciliation task for tradeId: {}", tradeId);
        
        try {
            reconciliationService.executeReconciliation(tradeId);
            log.info("Completed reconciliation task for tradeId: {}", tradeId);
        } catch (Exception e) {
            log.error("Error processing reconciliation task for tradeId: {}", tradeId, e);
            // In a production system, we might want to implement retry logic or dead-letter queue here
            // For example:
            // 1. Move message to a dead-letter queue
            // 2. Implement a retry mechanism with backoff
            // 3. Send notification about failed reconciliation
        }
    }
} 