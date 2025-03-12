package com.trading.reconciliation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.reconciliation.dto.TradeDataDto;
import com.trading.reconciliation.model.ReconciliationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class TradeReconciliationIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("trade_reconciliation_test")
            .withUsername("test")
            .withPassword("test");
    
    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management-alpine");
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
    }
    
    @Test
    void testTradeReconciliationFlow() throws Exception {
        // Create a unique trade ID for this test
        String tradeId = "T" + System.currentTimeMillis();
        LocalDateTime tradeDate = LocalDateTime.now();
        
        // Create matching trade data for System A and System B
        TradeDataDto systemATrade = TradeDataDto.builder()
                .tradeId(tradeId)
                .instrument("AAPL")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("150.75"))
                .tradeDate(tradeDate)
                .counterparty("BROKER_A")
                .build();
        
        TradeDataDto systemBTrade = TradeDataDto.builder()
                .tradeId(tradeId)
                .instrument("AAPL")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("150.75"))
                .tradeDate(tradeDate)
                .counterparty("BROKER_A")
                .build();
        
        // Submit trade data from System A
        mockMvc.perform(post("/trades/systemA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(systemATrade)))
                .andExpect(status().isAccepted());
        
        // Submit trade data from System B
        mockMvc.perform(post("/trades/systemB")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(systemBTrade)))
                .andExpect(status().isAccepted());
        
        // Wait for reconciliation to complete
        Thread.sleep(2000);
        
        // Check reconciliation status
        MvcResult result = mockMvc.perform(get("/reconciliations/" + tradeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId", is(tradeId)))
                .andExpect(jsonPath("$.status", is(ReconciliationStatus.MATCHED.toString())))
                .andReturn();
        
        // Create mismatched trade data
        String mismatchedTradeId = "T" + System.currentTimeMillis();
        TradeDataDto systemAMismatchedTrade = TradeDataDto.builder()
                .tradeId(mismatchedTradeId)
                .instrument("AAPL")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("150.75"))
                .tradeDate(tradeDate)
                .counterparty("BROKER_A")
                .build();
        
        TradeDataDto systemBMismatchedTrade = TradeDataDto.builder()
                .tradeId(mismatchedTradeId)
                .instrument("AAPL")
                .quantity(new BigDecimal("200")) // Different quantity
                .price(new BigDecimal("150.75"))
                .tradeDate(tradeDate)
                .counterparty("BROKER_A")
                .build();
        
        // Submit mismatched trade data
        mockMvc.perform(post("/trades/systemA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(systemAMismatchedTrade)))
                .andExpect(status().isAccepted());
        
        mockMvc.perform(post("/trades/systemB")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(systemBMismatchedTrade)))
                .andExpect(status().isAccepted());
        
        // Wait for reconciliation to complete
        Thread.sleep(2000);
        
        // Check reconciliation status for mismatched trade
        mockMvc.perform(get("/reconciliations/" + mismatchedTradeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId", is(mismatchedTradeId)))
                .andExpect(jsonPath("$.status", is(ReconciliationStatus.MISMATCHED.toString())));
    }
} 