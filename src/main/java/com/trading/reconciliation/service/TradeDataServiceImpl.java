package com.trading.reconciliation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.reconciliation.dto.TradeDataDto;
import com.trading.reconciliation.model.TradeData;
import com.trading.reconciliation.repository.TradeDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of TradeDataService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TradeDataServiceImpl implements TradeDataService {
    
    private final TradeDataRepository tradeDataRepository;
    private final ObjectMapper objectMapper;
    private final ReconciliationService reconciliationService;
    
    private static final String SYSTEM_A = "System A";
    private static final String SYSTEM_B = "System B";
    
    @Override
    public TradeData saveSystemATrade(TradeDataDto tradeDataDto) {
        log.info("Saving trade data from System A with tradeId: {}", tradeDataDto.getTradeId());
        TradeData tradeData = saveTradeData(tradeDataDto, SYSTEM_A);
        
        // Trigger reconciliation
        reconciliationService.triggerReconciliation(tradeDataDto.getTradeId());
        
        return tradeData;
    }
    
    @Override
    public TradeData saveSystemBTrade(TradeDataDto tradeDataDto) {
        log.info("Saving trade data from System B with tradeId: {}", tradeDataDto.getTradeId());
        TradeData tradeData = saveTradeData(tradeDataDto, SYSTEM_B);
        
        // Trigger reconciliation
        reconciliationService.triggerReconciliation(tradeDataDto.getTradeId());
        
        return tradeData;
    }
    
    @Override
    public Optional<TradeData> findByTradeIdAndSourceSystem(String tradeId, String sourceSystem) {
        return tradeDataRepository.findByTradeIdAndSourceSystem(tradeId, sourceSystem);
    }
    
    @Override
    public List<TradeData> findByTradeId(String tradeId) {
        return tradeDataRepository.findByTradeId(tradeId);
    }
    
    /**
     * Common method to save trade data
     */
    private TradeData saveTradeData(TradeDataDto tradeDataDto, String sourceSystem) {
        try {
            // Convert DTO to JSON string for raw data storage
            String rawData = objectMapper.writeValueAsString(tradeDataDto);
            
            // Check if trade data already exists for this tradeId and source system
            Optional<TradeData> existingTradeData = tradeDataRepository.findByTradeIdAndSourceSystem(
                    tradeDataDto.getTradeId(), sourceSystem);
            
            if (existingTradeData.isPresent()) {
                log.info("Updating existing trade data for tradeId: {} from {}", 
                        tradeDataDto.getTradeId(), sourceSystem);
                
                TradeData updatedTradeData = existingTradeData.get();
                updatedTradeData.setInstrument(tradeDataDto.getInstrument());
                updatedTradeData.setQuantity(tradeDataDto.getQuantity());
                updatedTradeData.setPrice(tradeDataDto.getPrice());
                updatedTradeData.setTradeDate(tradeDataDto.getTradeDate());
                updatedTradeData.setCounterparty(tradeDataDto.getCounterparty());
                updatedTradeData.setReceivedAt(LocalDateTime.now());
                updatedTradeData.setRawData(rawData);
                
                return tradeDataRepository.save(updatedTradeData);
            } else {
                // Create new trade data
                TradeData newTradeData = TradeData.builder()
                        .tradeId(tradeDataDto.getTradeId())
                        .instrument(tradeDataDto.getInstrument())
                        .quantity(tradeDataDto.getQuantity())
                        .price(tradeDataDto.getPrice())
                        .tradeDate(tradeDataDto.getTradeDate())
                        .counterparty(tradeDataDto.getCounterparty())
                        .sourceSystem(sourceSystem)
                        .receivedAt(LocalDateTime.now())
                        .rawData(rawData)
                        .build();
                
                return tradeDataRepository.save(newTradeData);
            }
        } catch (Exception e) {
            log.error("Error saving trade data for tradeId: {} from {}", 
                    tradeDataDto.getTradeId(), sourceSystem, e);
            throw new RuntimeException("Failed to save trade data", e);
        }
    }
} 