package com.bigdata.app.service;

import com.bigdata.app.config.BigDataProperties;
import com.bigdata.app.model.ProcessingResult;
import com.bigdata.app.model.Transaction;
import com.bigdata.app.model.TransactionStatus;
import com.bigdata.app.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Transaction service implementing Big Data processing patterns
 * Demonstrates async processing, batch operations, and Kafka integration
 */
@Service
@Transactional
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private BigDataProperties bigDataProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String TRANSACTION_TOPIC = "transactions-topic";
    private static final String ANALYTICS_TOPIC = "analytics-topic";
    
    /**
     * Save a single transaction and send to Kafka
     */
    public Transaction saveTransaction(Transaction transaction) {
        logger.debug("Saving transaction for customer: {}", transaction.getCustomerId());
        
        // Set timestamp if not provided
        if (transaction.getTimestamp() == null) {
            transaction.setTimestamp(LocalDateTime.now());
        }
        
        // Set default status if not provided
        if (transaction.getStatus() == null) {
            transaction.setStatus(TransactionStatus.PENDING);
        }
        
        // Save to database
        Transaction saved = transactionRepository.save(transaction);
        logger.info("Transaction saved with ID: {}", saved.getId());
        
        // Send to Kafka for real-time processing (if enabled)
        if (bigDataProperties.isKafkaEnabled()) {
            try {
                sendTransactionToKafka(saved);
            } catch (Exception e) {
                logger.warn("Failed to send transaction to Kafka: {}", e.getMessage());
            }
        }
        
        return saved;
    }
    
    /**
     * Get transactions by customer with pagination
     */
    public Page<Transaction> getTransactionsByCustomer(String customerId, Pageable pageable) {
        return transactionRepository.findByCustomerId(customerId, pageable);
    }
    
    /**
     * Get high value transactions
     */
    public List<Transaction> getHighValueTransactions(BigDecimal threshold) {
        return transactionRepository.findHighValueTransactions(threshold, TransactionStatus.APPROVED);
    }
    
    /**
     * Get transaction analytics for a date range
     */
    public Map<String, Object> getTransactionAnalytics(LocalDateTime start, LocalDateTime end) {
        logger.info("Generating analytics for period: {} to {}", start, end);
        
        List<Object[]> categorySummary = transactionRepository
            .getTransactionSummaryByCategory(start, end);
        
        List<Object[]> topCustomers = transactionRepository
            .getTopCustomers(start, end, 5);
        
        List<Object[]> dailyStats = transactionRepository
            .getDailyTransactionStats(start, end);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Category analysis
        Map<String, Map<String, Object>> categoryData = new HashMap<>();
        for (Object[] row : categorySummary) {
            Map<String, Object> data = new HashMap<>();
            data.put("count", row[1]);
            data.put("total", row[2]);
            data.put("average", row[3]);
            categoryData.put((String) row[0], data);
        }
        analytics.put("categoryAnalysis", categoryData);
        
        // Top customers
        List<Map<String, Object>> customerData = new ArrayList<>();
        for (Object[] row : topCustomers) {
            Map<String, Object> customer = new HashMap<>();
            customer.put("customerId", row[0]);
            customer.put("transactionCount", row[1]);
            customer.put("totalAmount", row[2]);
            customerData.add(customer);
        }
        analytics.put("topCustomers", customerData);
        
        // Daily statistics
        List<Map<String, Object>> dailyData = new ArrayList<>();
        for (Object[] row : dailyStats) {
            Map<String, Object> daily = new HashMap<>();
            daily.put("date", row[0]);
            daily.put("count", row[1]);
            daily.put("total", row[2]);
            daily.put("average", row[3]);
            dailyData.add(daily);
        }
        analytics.put("dailyStats", dailyData);
        
        analytics.put("generatedAt", LocalDateTime.now());
        analytics.put("period", Map.of("start", start, "end", end));
        
        return analytics;
    }
    
    /**
     * Process batch of transactions asynchronously
     */
    @Async("batchProcessingExecutor")
    public CompletableFuture<ProcessingResult> processBatchTransactions(List<Transaction> transactions) {
        logger.info("Starting batch processing of {} transactions", transactions.size());
        long startTime = System.currentTimeMillis();
        
        int processedCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();
        
        // Process transactions in batches
        int batchSize = bigDataProperties.getBatchSize();
        for (int i = 0; i < transactions.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, transactions.size());
            List<Transaction> batch = transactions.subList(i, endIndex);
            
            try {
                // Validate and enrich each transaction
                List<Transaction> processedBatch = batch.stream()
                    .map(this::validateAndEnrich)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                
                // Save batch
                transactionRepository.saveAll(processedBatch);
                processedCount += processedBatch.size();
                failedCount += (batch.size() - processedBatch.size());
                
                logger.debug("Processed batch {}-{}: {} successful, {} failed", 
                    i, endIndex, processedBatch.size(), batch.size() - processedBatch.size());
                
            } catch (Exception e) {
                logger.error("Failed to process batch {}-{}: {}", i, endIndex, e.getMessage());
                failedCount += batch.size();
                errors.add("Batch " + i + "-" + endIndex + ": " + e.getMessage());
            }
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        logger.info("Batch processing completed: {} processed, {} failed, {} ms", 
            processedCount, failedCount, processingTime);
        
        ProcessingResult result = new ProcessingResult(processedCount, failedCount, processingTime);
        result.setErrors(errors);
        
        return CompletableFuture.completedFuture(result);
    }
    
    /**
     * Detect suspicious transactions for fraud analysis
     */
    public List<Transaction> detectSuspiciousTransactions(String customerId) {
        LocalDateTime recentTime = LocalDateTime.now().minusHours(24);
        
        // Get customer's average transaction amount
        List<Transaction> recentTransactions = transactionRepository
            .findByTimestampBetween(recentTime, LocalDateTime.now())
            .stream()
            .filter(t -> t.getCustomerId().equals(customerId))
            .collect(Collectors.toList());
        
        if (recentTransactions.isEmpty()) {
            return Collections.emptyList();
        }
        
        BigDecimal avgAmount = recentTransactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(recentTransactions.size()), 2, java.math.RoundingMode.HALF_UP);
        
        // Suspicious if amount is 5x the average
        BigDecimal suspiciousThreshold = avgAmount.multiply(BigDecimal.valueOf(5));
        
        return transactionRepository.findSuspiciousTransactions(customerId, suspiciousThreshold, recentTime);
    }
    
    /**
     * Generate sample data for testing
     */
    public ProcessingResult generateSampleData(int count) {
        logger.info("Generating {} sample transactions", count);
        long startTime = System.currentTimeMillis();
        
        List<Transaction> sampleTransactions = new ArrayList<>();
        Random random = new Random();
        String[] categories = {"FOOD", "SHOPPING", "TRANSPORT", "ENTERTAINMENT", "UTILITIES"};
        String[] currencies = {"USD", "EUR", "GBP"};
        
        for (int i = 0; i < count; i++) {
            Transaction transaction = new Transaction();
            transaction.setCustomerId("CUST_" + (1000 + random.nextInt(9000)));
            transaction.setAmount(BigDecimal.valueOf(random.nextDouble() * 1000 + 1).setScale(2, java.math.RoundingMode.HALF_UP));
            transaction.setCategory(categories[random.nextInt(categories.length)]);
            transaction.setCurrency(currencies[random.nextInt(currencies.length)]);
            transaction.setMerchantId("MERCHANT_" + random.nextInt(100));
            transaction.setDescription("Sample transaction " + i);
            transaction.setStatus(TransactionStatus.values()[random.nextInt(TransactionStatus.values().length)]);
            transaction.setTimestamp(LocalDateTime.now().minusMinutes(random.nextInt(10080))); // Within last week
            
            sampleTransactions.add(transaction);
        }
        
        transactionRepository.saveAll(sampleTransactions);
        
        long processingTime = System.currentTimeMillis() - startTime;
        logger.info("Generated {} sample transactions in {} ms", count, processingTime);
        
        return new ProcessingResult(count, 0, processingTime);
    }
    
    private Transaction validateAndEnrich(Transaction transaction) {
        try {
            // Basic validation
            if (transaction.getCustomerId() == null || transaction.getAmount() == null) {
                return null;
            }
            
            // Set defaults
            if (transaction.getTimestamp() == null) {
                transaction.setTimestamp(LocalDateTime.now());
            }
            if (transaction.getStatus() == null) {
                transaction.setStatus(TransactionStatus.PENDING);
            }
            if (transaction.getCurrency() == null) {
                transaction.setCurrency("USD");
            }
            
            // Business logic enrichment
            if (transaction.getAmount().compareTo(BigDecimal.valueOf(10000)) > 0) {
                transaction.setStatus(TransactionStatus.PENDING); // Require manual approval
            }
            
            return transaction;
        } catch (Exception e) {
            logger.warn("Failed to validate transaction: {}", e.getMessage());
            return null;
        }
    }
    
    private void sendTransactionToKafka(Transaction transaction) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(transaction);
            kafkaTemplate.send(TRANSACTION_TOPIC, transaction.getCustomerId(), jsonMessage);
            logger.debug("Transaction sent to Kafka: {}", transaction.getId());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize transaction for Kafka: {}", e.getMessage());
            throw new RuntimeException("Kafka serialization failed", e);
        }
    }
}
