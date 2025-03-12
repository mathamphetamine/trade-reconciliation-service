package com.trading.reconciliation.model;

/**
 * Enum representing the possible statuses of a trade reconciliation
 */
public enum ReconciliationStatus {
    
    PENDING("Pending Reconciliation"),
    MATCHED("Matched"),
    MISMATCHED("Mismatched"),
    RECONCILIATION_TIMEOUT("Reconciliation Timeout"),
    ERROR("Error");
    
    private final String description;
    
    ReconciliationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 