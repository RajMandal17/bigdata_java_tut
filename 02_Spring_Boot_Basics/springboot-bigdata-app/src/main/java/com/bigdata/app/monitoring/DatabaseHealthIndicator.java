package com.bigdata.app.monitoring;

import com.bigdata.app.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for database connectivity
 * Demonstrates application monitoring for Big Data systems
 * Note: Simplified version - can be enhanced with proper HealthIndicator interface
 */
@Component
public class DatabaseHealthIndicator {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    /**
     * Simple health check method
     * Can be called via REST endpoint for monitoring
     */
    public boolean isHealthy() {
        try {
            // Simple database connectivity test
            transactionRepository.count();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get health status as string
     */
    public String getHealthStatus() {
        return isHealthy() ? "UP" : "DOWN";
    }
}
