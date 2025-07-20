package com.bigdata.kafka.consumer;

import com.bigdata.kafka.model.TransactionEvent;
import com.bigdata.kafka.service.TransactionProcessingService;
import com.bigdata.kafka.service.FraudDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class TransactionEventConsumer {
    
    @Autowired
    private TransactionProcessingService transactionService;
    
    @Autowired
    private FraudDetectionService fraudDetectionService;
    
    /**
     * Main transaction event consumer
     */
    @KafkaListener(
        topics = "${kafka.topics.transactions}",
        groupId = "transaction-processors",
        containerFactory = "transactionListenerContainerFactory"
    )
    public void consumeTransactionEvent(
            @Payload TransactionEvent transaction,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received transaction event: {} from partition: {}, offset: {}, timestamp: {}",
                    transaction.getTransactionId(), partition, offset, timestamp);
            
            // Process the transaction
            transactionService.processTransaction(transaction);
            
            // Manual acknowledgment after successful processing
            acknowledgment.acknowledge();
            
            log.debug("Transaction processed successfully: {}", transaction.getTransactionId());
            
        } catch (Exception e) {
            log.error("Error processing transaction event: {} from partition: {}, offset: {}",
                    transaction.getTransactionId(), partition, offset, e);
            
            // Don't acknowledge - message will be retried
            // Could also implement custom error handling here
        }
    }
    
    /**
     * Fraud detection consumer
     */
    @KafkaListener(
        topics = "${kafka.topics.transactions}",
        groupId = "fraud-detection-group",
        containerFactory = "transactionListenerContainerFactory"
    )
    public void consumeForFraudDetection(
            @Payload TransactionEvent transaction,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("Processing transaction for fraud detection: {}", transaction.getTransactionId());
            
            // Perform fraud detection
            boolean isSuspicious = fraudDetectionService.analyzeFraud(transaction);
            
            if (isSuspicious) {
                log.warn("Suspicious transaction detected: {} for customer: {}",
                        transaction.getTransactionId(), transaction.getCustomerId());
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error in fraud detection for transaction: {}", 
                    transaction.getTransactionId(), e);
        }
    }
    
    /**
     * High-value transaction processor
     */
    @KafkaListener(
        topics = "${kafka.topics.transactions}",
        groupId = "high-value-processors",
        containerFactory = "transactionListenerContainerFactory",
        condition = "#{T(java.math.BigDecimal).valueOf(#root.payload.amount).compareTo(T(java.math.BigDecimal).valueOf(10000)) > 0}"
    )
    public void consumeHighValueTransactions(
            @Payload TransactionEvent transaction,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Processing high-value transaction: {} amount: {}",
                    transaction.getTransactionId(), transaction.getAmount());
            
            // Special processing for high-value transactions
            transactionService.processHighValueTransaction(transaction);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing high-value transaction: {}", 
                    transaction.getTransactionId(), e);
        }
    }
    
    /**
     * Batch consumer for analytics
     */
    @KafkaListener(
        topics = "${kafka.topics.transactions}",
        groupId = "analytics-batch-processors",
        containerFactory = "batchListenerContainerFactory"
    )
    public void consumeTransactionBatch(
            List<ConsumerRecord<String, TransactionEvent>> records,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received batch of {} transaction events for analytics", records.size());
            
            List<TransactionEvent> transactions = records.stream()
                .map(ConsumerRecord::value)
                .toList();
            
            // Process batch for analytics
            transactionService.processBatchForAnalytics(transactions);
            
            acknowledgment.acknowledge();
            
            log.debug("Batch processing completed for {} transactions", transactions.size());
            
        } catch (Exception e) {
            log.error("Error processing transaction batch", e);
        }
    }
    
    /**
     * Regional transaction processor (partition-specific)
     */
    @KafkaListener(
        topics = "${kafka.topics.transactions}",
        groupId = "regional-processors",
        topicPartitions = @org.springframework.kafka.annotation.TopicPartition(
            topic = "${kafka.topics.transactions}",
            partitions = {"0", "1"}  // Process only specific partitions
        ),
        containerFactory = "transactionListenerContainerFactory"
    )
    public void consumeRegionalTransactions(
            @Payload TransactionEvent transaction,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("Processing regional transaction: {} from partition: {}",
                    transaction.getTransactionId(), partition);
            
            // Region-specific processing based on partition
            String region = partition == 0 ? "US" : "EU";
            transactionService.processRegionalTransaction(transaction, region);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing regional transaction: {} from partition: {}",
                    transaction.getTransactionId(), partition, e);
        }
    }
    
    /**
     * Conditional consumer based on transaction status
     */
    @KafkaListener(
        topics = "${kafka.topics.transactions}",
        groupId = "declined-transaction-processors",
        containerFactory = "transactionListenerContainerFactory",
        condition = "#{T(com.bigdata.kafka.model.TransactionEvent.TransactionStatus).DECLINED.equals(#root.payload.status)}"
    )
    public void consumeDeclinedTransactions(
            @Payload TransactionEvent transaction,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Processing declined transaction: {} for customer: {}",
                    transaction.getTransactionId(), transaction.getCustomerId());
            
            // Handle declined transaction
            transactionService.handleDeclinedTransaction(transaction);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing declined transaction: {}", 
                    transaction.getTransactionId(), e);
        }
    }
    
    /**
     * Real-time transaction monitoring
     */
    @KafkaListener(
        topics = "${kafka.topics.transactions}",
        groupId = "real-time-monitoring",
        containerFactory = "transactionListenerContainerFactory"
    )
    public void monitorTransactions(
            @Payload TransactionEvent transaction,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long kafkaTimestamp,
            Acknowledgment acknowledgment) {
        
        try {
            // Calculate processing latency
            long currentTime = System.currentTimeMillis();
            long latency = currentTime - kafkaTimestamp;
            
            if (latency > 5000) { // Alert if processing takes more than 5 seconds
                log.warn("High latency detected for transaction: {} - {}ms",
                        transaction.getTransactionId(), latency);
            }
            
            // Update monitoring metrics
            transactionService.updateMonitoringMetrics(transaction, latency);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error in transaction monitoring: {}", 
                    transaction.getTransactionId(), e);
        }
    }
}
