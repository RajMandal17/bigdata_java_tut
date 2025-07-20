package com.bigdata.demo.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for processing results - demonstrates immutable design pattern
 */
public class ProcessingResult {
    private final int processedRecords;
    private final int failedRecords;
    private final long processingTimeMs;
    private final BigDecimal totalAmount;
    private final List<String> errors;
    
    public ProcessingResult(int processedRecords, int failedRecords, 
                          long processingTimeMs, BigDecimal totalAmount, 
                          List<String> errors) {
        this.processedRecords = processedRecords;
        this.failedRecords = failedRecords;
        this.processingTimeMs = processingTimeMs;
        this.totalAmount = totalAmount;
        this.errors = errors;
    }
    
    // Factory methods for common use cases
    public static ProcessingResult empty() {
        return new ProcessingResult(0, 0, 0L, BigDecimal.ZERO, List.of());
    }
    
    public static ProcessingResult success(int processed, long timeMs) {
        return new ProcessingResult(processed, 0, timeMs, BigDecimal.ZERO, List.of());
    }
    
    public static ProcessingResult success(String message, long timeMs) {
        return new ProcessingResult(1, 0, timeMs, BigDecimal.ZERO, List.of(message));
    }
    
    public static ProcessingResult error(String errorMessage) {
        return new ProcessingResult(0, 1, 0L, BigDecimal.ZERO, List.of(errorMessage));
    }
    
    public static ProcessingResult withErrors(int processed, int failed, 
                                            List<String> errors, long timeMs) {
        return new ProcessingResult(processed, failed, timeMs, BigDecimal.ZERO, errors);
    }
    
    // Getters only (immutable)
    public int getProcessedRecords() { return processedRecords; }
    public int getFailedRecords() { return failedRecords; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<String> getErrors() { return errors; }
    public int getTotalRecords() { return processedRecords + failedRecords; }
    public double getSuccessRate() { 
        return getTotalRecords() > 0 ? (double) processedRecords / getTotalRecords() * 100 : 0; 
    }
    
    @Override
    public String toString() {
        return String.format(
            "ProcessingResult{processed=%d, failed=%d, time=%dms, total=%.2f, successRate=%.1f%%}",
            processedRecords, failedRecords, processingTimeMs, totalAmount, getSuccessRate()
        );
    }
}
