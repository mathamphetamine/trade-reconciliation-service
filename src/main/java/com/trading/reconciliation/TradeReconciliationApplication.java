package com.trading.reconciliation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Trade Reconciliation Service Application
 * Main entry point for the Trade Data Reconciliation Microservice
 * 
 * This service automates the reconciliation of trade data from different systems:
 * 1. Receives trade data asynchronously via RabbitMQ
 * 2. Stores raw trade data in the PostgreSQL database
 * 3. Compares trade data from different systems to identify discrepancies
 * 4. Provides RESTful APIs for trade submission and reconciliation status queries
 * 
 * The @EnableScheduling annotation enables the scheduled task that processes 
 * pending reconciliations that have timed out.
 */
@SpringBootApplication
@EnableScheduling
public class TradeReconciliationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeReconciliationApplication.class, args);
    }
} 