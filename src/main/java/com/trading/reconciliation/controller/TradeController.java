package com.trading.reconciliation.controller;

import com.trading.reconciliation.dto.TradeDataDto;
import com.trading.reconciliation.service.TradeDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for trade data submission
 * 
 * This controller provides endpoints for submitting trade data from different systems.
 * Trade data is sent as JSON and validated using Jakarta Bean Validation annotations.
 * 
 * After receiving the trade data, the controller forwards it to the TradeDataService,
 * which stores it in the database and triggers the reconciliation process.
 * 
 * The controller returns an HTTP 202 (Accepted) response to indicate that the
 * trade data has been accepted for processing, but the reconciliation may
 * not be complete yet.
 */
@RestController
@RequestMapping("/trades")
@Slf4j
@RequiredArgsConstructor
public class TradeController {
    
    private final TradeDataService tradeDataService;
    
    /**
     * Submit trade data from System A
     * 
     * This endpoint receives trade data from System A, validates it,
     * and forwards it to the TradeDataService for processing.
     * 
     * Example request:
     * POST /trades/systemA
     * {
     *   "tradeId": "T123456",
     *   "instrument": "AAPL",
     *   "quantity": 100,
     *   "price": 150.75,
     *   "tradeDate": "2023-06-15T10:30:00",
     *   "counterparty": "BROKER_A"
     * }
     * 
     * @param tradeDataDto The trade data DTO with trade details
     * @return HTTP 202 Accepted response
     */
    @PostMapping("/systemA")
    public ResponseEntity<Void> submitSystemATrade(@Valid @RequestBody TradeDataDto tradeDataDto) {
        log.info("Received trade data from System A with tradeId: {}", tradeDataDto.getTradeId());
        
        try {
            tradeDataService.saveSystemATrade(tradeDataDto);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (Exception e) {
            log.error("Error processing trade data from System A", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Submit trade data from System B
     * 
     * This endpoint receives trade data from System B, validates it,
     * and forwards it to the TradeDataService for processing.
     * 
     * The request format is identical to the System A endpoint.
     * 
     * @param tradeDataDto The trade data DTO with trade details
     * @return HTTP 202 Accepted response
     */
    @PostMapping("/systemB")
    public ResponseEntity<Void> submitSystemBTrade(@Valid @RequestBody TradeDataDto tradeDataDto) {
        log.info("Received trade data from System B with tradeId: {}", tradeDataDto.getTradeId());
        
        try {
            tradeDataService.saveSystemBTrade(tradeDataDto);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (Exception e) {
            log.error("Error processing trade data from System B", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 