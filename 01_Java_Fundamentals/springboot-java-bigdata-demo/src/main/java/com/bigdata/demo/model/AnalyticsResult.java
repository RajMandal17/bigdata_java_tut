package com.bigdata.demo.model;

import java.math.BigDecimal;

/**
 * Analytics result class demonstrating builder pattern
 */
public class AnalyticsResult {
    private final String category;
    private final long transactionCount;
    private final BigDecimal totalAmount;
    private final BigDecimal averageAmount;
    private final BigDecimal minAmount;
    private final BigDecimal maxAmount;
    
    private AnalyticsResult(Builder builder) {
        this.category = builder.category;
        this.transactionCount = builder.transactionCount;
        this.totalAmount = builder.totalAmount;
        this.averageAmount = builder.averageAmount;
        this.minAmount = builder.minAmount;
        this.maxAmount = builder.maxAmount;
    }
    
    // Getters
    public String getCategory() { return category; }
    public long getTransactionCount() { return transactionCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getAverageAmount() { return averageAmount; }
    public BigDecimal getMinAmount() { return minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    
    // Builder pattern
    public static class Builder {
        private String category;
        private long transactionCount;
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private BigDecimal averageAmount = BigDecimal.ZERO;
        private BigDecimal minAmount = BigDecimal.ZERO;
        private BigDecimal maxAmount = BigDecimal.ZERO;
        
        public Builder category(String category) { this.category = category; return this; }
        public Builder transactionCount(long count) { this.transactionCount = count; return this; }
        public Builder totalAmount(BigDecimal total) { this.totalAmount = total; return this; }
        public Builder averageAmount(BigDecimal average) { this.averageAmount = average; return this; }
        public Builder minAmount(BigDecimal min) { this.minAmount = min; return this; }
        public Builder maxAmount(BigDecimal max) { this.maxAmount = max; return this; }
        
        public AnalyticsResult build() { return new AnalyticsResult(this); }
    }
    
    @Override
    public String toString() {
        return String.format(
            "AnalyticsResult{category='%s', count=%d, total=%.2f, avg=%.2f, min=%.2f, max=%.2f}",
            category, transactionCount, totalAmount, averageAmount, minAmount, maxAmount
        );
    }
}
