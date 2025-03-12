package com.trading.reconciliation.service;

import com.trading.reconciliation.dto.ReconciliationResponseDto;
import com.trading.reconciliation.model.ReconciliationStatus;
import com.trading.reconciliation.model.TradeData;
import com.trading.reconciliation.model.TradeReconciliation;
import com.trading.reconciliation.repository.TradeDataRepository;
import com.trading.reconciliation.repository.TradeReconciliationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of ReconciliationService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReconciliationServiceImpl implements ReconciliationService {
    
    private final TradeDataRepository tradeDataRepository;
    private final TradeReconciliationRepository reconciliationRepository;
    private final RabbitTemplate rabbitTemplate;
    
    private static final String SYSTEM_A = "System A";
    private static final String SYSTEM_B = "System B";
    
    @Value("${reconciliation.queue.reconciliation-tasks}")
    private String reconciliationTasksQueue;
    
    @Value("${reconciliation.timeout-minutes}")
    private int reconciliationTimeoutMinutes;
    
    @Override
    public void triggerReconciliation(String tradeId) {
        log.info("Triggering reconciliation for tradeId: {}", tradeId);
        
        // Send message to reconciliation queue
        rabbitTemplate.convertAndSend(reconciliationTasksQueue, tradeId);
    }
    
    @Override
    @Transactional
    public void executeReconciliation(String tradeId) {
        log.info("Executing reconciliation for tradeId: {}", tradeId);
        
        try {
            // Get trade data from both systems
            Optional<TradeData> systemATradeOpt = tradeDataRepository.findByTradeIdAndSourceSystem(tradeId, SYSTEM_A);
            Optional<TradeData> systemBTradeOpt = tradeDataRepository.findByTradeIdAndSourceSystem(tradeId, SYSTEM_B);
            
            // Check if we have data from both systems
            if (systemATradeOpt.isPresent() && systemBTradeOpt.isPresent()) {
                TradeData systemATrade = systemATradeOpt.get();
                TradeData systemBTrade = systemBTradeOpt.get();
                
                // Compare the trades
                List<String> discrepancies = compareTradeData(systemATrade, systemBTrade);
                
                // Get or create reconciliation record
                TradeReconciliation reconciliation = reconciliationRepository.findByTradeId(tradeId)
                        .orElse(new TradeReconciliation());
                
                // Update reconciliation record
                reconciliation.setTradeId(tradeId);
                reconciliation.setSystemATradeId(systemATrade.getId());
                reconciliation.setSystemBTradeId(systemBTrade.getId());
                reconciliation.setLastReconciliationAttempt(LocalDateTime.now());
                
                if (discrepancies.isEmpty()) {
                    // No discrepancies found, mark as MATCHED
                    reconciliation.setStatus(ReconciliationStatus.MATCHED);
                    reconciliation.setDetails("Trades matched successfully");
                    log.info("Trades matched for tradeId: {}", tradeId);
                } else {
                    // Discrepancies found, mark as MISMATCHED
                    reconciliation.setStatus(ReconciliationStatus.MISMATCHED);
                    reconciliation.setDetails("Discrepancies found: " + String.join(", ", discrepancies));
                    log.info("Trades mismatched for tradeId: {}. Discrepancies: {}", 
                            tradeId, String.join(", ", discrepancies));
                }
                
                // Save reconciliation record
                reconciliationRepository.save(reconciliation);
            } else if (systemATradeOpt.isPresent() || systemBTradeOpt.isPresent()) {
                // We have data from only one system, mark as PENDING
                
                // Get or create reconciliation record
                TradeReconciliation reconciliation = reconciliationRepository.findByTradeId(tradeId)
                        .orElse(new TradeReconciliation());
                
                // Update reconciliation record
                reconciliation.setTradeId(tradeId);
                reconciliation.setStatus(ReconciliationStatus.PENDING);
                reconciliation.setLastReconciliationAttempt(LocalDateTime.now());
                
                if (systemATradeOpt.isPresent()) {
                    TradeData systemATrade = systemATradeOpt.get();
                    reconciliation.setSystemATradeId(systemATrade.getId());
                    reconciliation.setDetails("Waiting for data from System B");
                    log.info("Pending reconciliation for tradeId: {}. Waiting for data from System B", tradeId);
                } else {
                    TradeData systemBTrade = systemBTradeOpt.get();
                    reconciliation.setSystemBTradeId(systemBTrade.getId());
                    reconciliation.setDetails("Waiting for data from System A");
                    log.info("Pending reconciliation for tradeId: {}. Waiting for data from System A", tradeId);
                }
                
                // Save reconciliation record
                reconciliationRepository.save(reconciliation);
            } else {
                // This should not happen as reconciliation is triggered when data is received from either system
                log.warn("No trade data found for tradeId: {} in either system", tradeId);
            }
        } catch (Exception e) {
            log.error("Error executing reconciliation for tradeId: {}", tradeId, e);
            
            // Get or create reconciliation record and mark as ERROR
            TradeReconciliation reconciliation = reconciliationRepository.findByTradeId(tradeId)
                    .orElse(new TradeReconciliation());
            
            reconciliation.setTradeId(tradeId);
            reconciliation.setStatus(ReconciliationStatus.ERROR);
            reconciliation.setDetails("Error executing reconciliation: " + e.getMessage());
            reconciliation.setLastReconciliationAttempt(LocalDateTime.now());
            
            // Save reconciliation record
            reconciliationRepository.save(reconciliation);
        }
    }
    
    /**
     * Compare two trade data records and return a list of discrepancies
     */
    private List<String> compareTradeData(TradeData systemATrade, TradeData systemBTrade) {
        List<String> discrepancies = new ArrayList<>();
        
        // Compare instrument
        if (!systemATrade.getInstrument().equals(systemBTrade.getInstrument())) {
            discrepancies.add("Instrument mismatch: " + systemATrade.getInstrument() + " vs " + systemBTrade.getInstrument());
        }
        
        // Compare quantity (use compareTo for BigDecimal)
        if (systemATrade.getQuantity().compareTo(systemBTrade.getQuantity()) != 0) {
            discrepancies.add("Quantity mismatch: " + systemATrade.getQuantity() + " vs " + systemBTrade.getQuantity());
        }
        
        // Compare price (use compareTo for BigDecimal)
        if (systemATrade.getPrice().compareTo(systemBTrade.getPrice()) != 0) {
            discrepancies.add("Price mismatch: " + systemATrade.getPrice() + " vs " + systemBTrade.getPrice());
        }
        
        // Compare trade date
        if (!systemATrade.getTradeDate().equals(systemBTrade.getTradeDate())) {
            discrepancies.add("Trade date mismatch: " + systemATrade.getTradeDate() + " vs " + systemBTrade.getTradeDate());
        }
        
        // Compare counterparty
        if (!systemATrade.getCounterparty().equals(systemBTrade.getCounterparty())) {
            discrepancies.add("Counterparty mismatch: " + systemATrade.getCounterparty() + " vs " + systemBTrade.getCounterparty());
        }
        
        return discrepancies;
    }
    
    @Override
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void processTimeouts() {
        log.info("Processing reconciliation timeouts");
        
        // Calculate timeout threshold
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(reconciliationTimeoutMinutes);
        
        // Find pending reconciliations that have timed out
        List<TradeReconciliation> timedOutReconciliations = 
                reconciliationRepository.findTimedOutReconciliations(ReconciliationStatus.PENDING, timeoutThreshold);
        
        log.info("Found {} pending reconciliations that have timed out", timedOutReconciliations.size());
        
        // Process each timed out reconciliation
        for (TradeReconciliation reconciliation : timedOutReconciliations) {
            log.info("Processing timeout for tradeId: {}", reconciliation.getTradeId());
            
            // Update reconciliation status
            reconciliation.setStatus(ReconciliationStatus.RECONCILIATION_TIMEOUT);
            reconciliation.setDetails("Reconciliation timed out after " + reconciliationTimeoutMinutes + " minutes");
            reconciliation.setLastReconciliationAttempt(LocalDateTime.now());
            
            // Save updated reconciliation
            reconciliationRepository.save(reconciliation);
        }
    }
    
    @Override
    public Optional<ReconciliationResponseDto> getReconciliationStatus(String tradeId) {
        log.info("Getting reconciliation status for tradeId: {}", tradeId);
        
        return reconciliationRepository.findByTradeId(tradeId)
                .map(this::mapToResponseDto);
    }
    
    @Override
    public Page<ReconciliationResponseDto> getReconciliations(String status, Pageable pageable) {
        log.info("Getting reconciliations with status filter: {}", status);
        
        Page<TradeReconciliation> reconciliations;
        
        if (status != null && !status.isEmpty()) {
            try {
                ReconciliationStatus reconciliationStatus = ReconciliationStatus.valueOf(status.toUpperCase());
                reconciliations = reconciliationRepository.findByStatus(reconciliationStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value: {}. Returning all reconciliations.", status);
                reconciliations = reconciliationRepository.findAll(pageable);
            }
        } else {
            reconciliations = reconciliationRepository.findAll(pageable);
        }
        
        return reconciliations.map(this::mapToResponseDto);
    }
    
    /**
     * Map TradeReconciliation entity to ReconciliationResponseDto
     */
    private ReconciliationResponseDto mapToResponseDto(TradeReconciliation reconciliation) {
        return ReconciliationResponseDto.builder()
                .tradeId(reconciliation.getTradeId())
                .status(reconciliation.getStatus())
                .statusDescription(reconciliation.getStatus().getDescription())
                .details(reconciliation.getDetails())
                .createdAt(reconciliation.getCreatedAt())
                .updatedAt(reconciliation.getUpdatedAt())
                .lastReconciliationAttempt(reconciliation.getLastReconciliationAttempt())
                .build();
    }
} 