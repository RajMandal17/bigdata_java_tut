package com.bigdata.app.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Custom metrics for transaction processing
 * Demonstrates application metrics for Big Data monitoring
 */
@Component
public class TransactionMetrics {
    
    private final Counter transactionCounter;
    private final Counter failedTransactionCounter;
    private final Timer transactionProcessingTimer;
    private final Counter batchProcessingCounter;
    
    public TransactionMetrics(MeterRegistry meterRegistry) {
        this.transactionCounter = meterRegistry.counter("transactions.processed", "type", "individual");
        this.failedTransactionCounter = meterRegistry.counter("transactions.failed", "type", "individual");
        this.transactionProcessingTimer = meterRegistry.timer("transactions.processing.time");
        this.batchProcessingCounter = meterRegistry.counter("transactions.processed", "type", "batch");
    }
    
    public void incrementTransactionCount() {
        transactionCounter.increment();
    }
    
    public void incrementFailedTransactionCount() {
        failedTransactionCounter.increment();
    }
    
    public void incrementBatchProcessingCount(int count) {
        batchProcessingCounter.increment(count);
    }
    
    public void recordProcessingTime(Duration duration) {
        transactionProcessingTimer.record(duration);
    }
    
    public Timer.Sample startTimer() {
        return Timer.start();
    }
    
    public void stopTimer(Timer.Sample sample) {
        sample.stop(transactionProcessingTimer);
    }
}
