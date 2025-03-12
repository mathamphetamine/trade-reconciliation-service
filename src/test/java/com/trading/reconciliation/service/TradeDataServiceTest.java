package com.trading.reconciliation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.reconciliation.dto.TradeDataDto;
import com.trading.reconciliation.model.TradeData;
import com.trading.reconciliation.repository.TradeDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TradeDataServiceTest {
    
    @Mock
    private TradeDataRepository tradeDataRepository;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private ReconciliationService reconciliationService;
    
    @InjectMocks
    private TradeDataServiceImpl tradeDataService;
    
    private TradeDataDto tradeDataDto;
    private TradeData tradeData;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        LocalDateTime tradeDate = LocalDateTime.now();
        
        tradeDataDto = TradeDataDto.builder()
                .tradeId("T123456")
                .instrument("AAPL")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("150.75"))
                .tradeDate(tradeDate)
                .counterparty("BROKER_A")
                .build();
        
        tradeData = TradeData.builder()
                .id(1L)
                .tradeId("T123456")
                .instrument("AAPL")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("150.75"))
                .tradeDate(tradeDate)
                .counterparty("BROKER_A")
                .sourceSystem("System A")
                .receivedAt(LocalDateTime.now())
                .rawData("{\"tradeId\":\"T123456\",\"instrument\":\"AAPL\",\"quantity\":100,\"price\":150.75,\"tradeDate\":\"" + tradeDate + "\",\"counterparty\":\"BROKER_A\"}")
                .build();
    }
    
    @Test
    void testSaveSystemATrade_NewTrade() throws Exception {
        // Arrange
        when(objectMapper.writeValueAsString(any(TradeDataDto.class))).thenReturn("{}");
        when(tradeDataRepository.findByTradeIdAndSourceSystem(anyString(), anyString())).thenReturn(Optional.empty());
        when(tradeDataRepository.save(any(TradeData.class))).thenReturn(tradeData);
        doNothing().when(reconciliationService).triggerReconciliation(anyString());
        
        // Act
        TradeData result = tradeDataService.saveSystemATrade(tradeDataDto);
        
        // Assert
        assertNotNull(result);
        assertEquals("T123456", result.getTradeId());
        assertEquals("AAPL", result.getInstrument());
        assertEquals(0, new BigDecimal("100").compareTo(result.getQuantity()));
        assertEquals(0, new BigDecimal("150.75").compareTo(result.getPrice()));
        assertEquals("BROKER_A", result.getCounterparty());
        assertEquals("System A", result.getSourceSystem());
        
        verify(tradeDataRepository).findByTradeIdAndSourceSystem("T123456", "System A");
        verify(tradeDataRepository).save(any(TradeData.class));
        verify(reconciliationService).triggerReconciliation("T123456");
    }
    
    @Test
    void testSaveSystemATrade_ExistingTrade() throws Exception {
        // Arrange
        when(objectMapper.writeValueAsString(any(TradeDataDto.class))).thenReturn("{}");
        when(tradeDataRepository.findByTradeIdAndSourceSystem(anyString(), anyString())).thenReturn(Optional.of(tradeData));
        when(tradeDataRepository.save(any(TradeData.class))).thenReturn(tradeData);
        doNothing().when(reconciliationService).triggerReconciliation(anyString());
        
        // Act
        TradeData result = tradeDataService.saveSystemATrade(tradeDataDto);
        
        // Assert
        assertNotNull(result);
        assertEquals("T123456", result.getTradeId());
        
        verify(tradeDataRepository).findByTradeIdAndSourceSystem("T123456", "System A");
        verify(tradeDataRepository).save(any(TradeData.class));
        verify(reconciliationService).triggerReconciliation("T123456");
    }
    
    @Test
    void testSaveSystemBTrade() throws Exception {
        // Arrange
        when(objectMapper.writeValueAsString(any(TradeDataDto.class))).thenReturn("{}");
        when(tradeDataRepository.findByTradeIdAndSourceSystem(anyString(), anyString())).thenReturn(Optional.empty());
        when(tradeDataRepository.save(any(TradeData.class))).thenReturn(tradeData);
        doNothing().when(reconciliationService).triggerReconciliation(anyString());
        
        // Act
        TradeData result = tradeDataService.saveSystemBTrade(tradeDataDto);
        
        // Assert
        assertNotNull(result);
        assertEquals("T123456", result.getTradeId());
        
        verify(tradeDataRepository).findByTradeIdAndSourceSystem("T123456", "System B");
        verify(tradeDataRepository).save(any(TradeData.class));
        verify(reconciliationService).triggerReconciliation("T123456");
    }
    
    @Test
    void testFindByTradeIdAndSourceSystem() {
        // Arrange
        when(tradeDataRepository.findByTradeIdAndSourceSystem(anyString(), anyString())).thenReturn(Optional.of(tradeData));
        
        // Act
        Optional<TradeData> result = tradeDataService.findByTradeIdAndSourceSystem("T123456", "System A");
        
        // Assert
        assertNotNull(result);
        assertEquals(true, result.isPresent());
        assertEquals("T123456", result.get().getTradeId());
        
        verify(tradeDataRepository).findByTradeIdAndSourceSystem("T123456", "System A");
    }
} 