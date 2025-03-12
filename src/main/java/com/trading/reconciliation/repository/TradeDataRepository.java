package com.trading.reconciliation.repository;

import com.trading.reconciliation.model.TradeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TradeData entity
 */
@Repository
public interface TradeDataRepository extends JpaRepository<TradeData, Long> {
    
    /**
     * Find trade data by tradeId and sourceSystem
     * @param tradeId The trade identifier
     * @param sourceSystem The source system (e.g., "System A", "System B")
     * @return An Optional containing the trade data if found
     */
    Optional<TradeData> findByTradeIdAndSourceSystem(String tradeId, String sourceSystem);
    
    /**
     * Find all trade data by tradeId
     * @param tradeId The trade identifier
     * @return List of trade data with the specified tradeId
     */
    List<TradeData> findByTradeId(String tradeId);
    
    /**
     * Find all trade data by sourceSystem
     * @param sourceSystem The source system (e.g., "System A", "System B")
     * @return List of trade data from the specified source system
     */
    List<TradeData> findBySourceSystem(String sourceSystem);
} 