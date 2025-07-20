package com.bigdata.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Processing result wrapper for API responses
 * Provides consistent response format across all endpoints
 */
public class ProcessingResult {
    private long processedRecords;
    private long failedRecords;
    private long totalRecords;
    private long processingTimeMs;
    private BigDecimal totalAmount;
    private double successRate;
    private List<String> errors;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;

    public ProcessingResult() {
        this.timestamp = LocalDateTime.now();
    }

    public ProcessingResult(long processedRecords, long failedRecords, long processingTimeMs) {
        this();
        this.processedRecords = processedRecords;
        this.failedRecords = failedRecords;
        this.totalRecords = processedRecords + failedRecords;
        this.processingTimeMs = processingTimeMs;
        this.successRate = totalRecords > 0 ? (double) processedRecords / totalRecords * 100 : 0;
    }

    // Getters and Setters
    public long getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(long processedRecords) {
        this.processedRecords = processedRecords;
    }

    public long getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(long failedRecords) {
        this.failedRecords = failedRecords;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
