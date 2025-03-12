package com.trading.reconciliation.service;

import com.trading.reconciliation.dto.TradeDataDto;
import com.trading.reconciliation.model.TradeData;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for trade data operations
 */
public interface TradeDataService {
    
    /**
     * Save trade data from System A
     * @param tradeDataDto The trade data DTO
     * @return The saved TradeData entity
     */
    TradeData saveSystemATrade(TradeDataDto tradeDataDto);
    
    /**
     * Save trade data from System B
     * @param tradeDataDto The trade data DTO
     * @return The saved TradeData entity
     */
    TradeData saveSystemBTrade(TradeDataDto tradeDataDto);
    
    /**
     * Find trade data by trade ID and source system
     * @param tradeId The trade identifier
     * @param sourceSystem The source system
     * @return Optional containing the trade data if found
     */
    Optional<TradeData> findByTradeIdAndSourceSystem(String tradeId, String sourceSystem);
    
    /**
     * Find all trade data for a given trade ID
     * @param tradeId The trade identifier
     * @return List of trade data with the specified trade ID
     */
    List<TradeData> findByTradeId(String tradeId);
} 