package com.bigdata.kafka.service;

import com.bigdata.kafka.model.TransactionEvent;
import com.bigdata.kafka.producer.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Transaction Processing Service
 * Handles business logic for transaction processing and validation
 */
@Service
@Transactional
public class TransactionProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionProcessingService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Process incoming transaction event
     */
    public void processTransaction(TransactionEvent transaction) {
        logger.info("Processing transaction: {}", transaction.getTransactionId());

        try {
            // 1. Validate transaction
            validateTransaction(transaction);

            // 2. Check for fraud
            boolean isFraudulent = fraudDetectionService.checkForFraud(transaction);
            if (isFraudulent) {
                handleFraudulentTransaction(transaction);
                return;
            }

            // 3. Save to database
            saveTransactionToDatabase(transaction);

            // 4. Update customer balance/limits
            updateCustomerData(transaction);

            // 5. Send notifications if needed
            sendNotificationsIfNeeded(transaction);

            // 6. Update transaction status
            updateTransactionStatus(transaction.getTransactionId(), "COMPLETED");

            logger.info("Successfully processed transaction: {}", transaction.getTransactionId());

        } catch (Exception e) {
            logger.error("Error processing transaction: {}", transaction.getTransactionId(), e);
            updateTransactionStatus(transaction.getTransactionId(), "FAILED");
            throw new RuntimeException("Transaction processing failed", e);
        }
    }

    /**
     * Process batch of transactions
     */
    public void processBatchTransactions(List<TransactionEvent> transactions) {
        logger.info("Processing batch of {} transactions", transactions.size());

        for (TransactionEvent transaction : transactions) {
            try {
                processTransaction(transaction);
            } catch (Exception e) {
                logger.error("Error processing transaction in batch: {}", 
                           transaction.getTransactionId(), e);
                // Continue processing other transactions
            }
        }
    }

    /**
     * Validate transaction data
     */
    private void validateTransaction(TransactionEvent transaction) {
        if (transaction.getTransactionId() == null || transaction.getTransactionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID is required");
        }

        if (transaction.getCustomerId() == null || transaction.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        if (transaction.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            throw new IllegalArgumentException("Transaction amount exceeds maximum limit");
        }

        if (transaction.getCategory() == null || transaction.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction category is required");
        }
    }

    /**
     * Save transaction to database
     */
    private void saveTransactionToDatabase(TransactionEvent transaction) {
        String sql = """
            INSERT INTO transactions (
                transaction_id, customer_id, amount, category, 
                transaction_time, status, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (transaction_id) DO NOTHING
            """;

        int rowsAffected = jdbcTemplate.update(sql,
            transaction.getTransactionId(),
            transaction.getCustomerId(),
            transaction.getAmount(),
            transaction.getCategory(),
            transaction.getTimestamp(),
            "PROCESSING",
            LocalDateTime.now()
        );

        if (rowsAffected == 0) {
            logger.warn("Transaction already exists: {}", transaction.getTransactionId());
        }
    }

    /**
     * Update customer data (balance, spending limits, etc.)
     */
    private void updateCustomerData(TransactionEvent transaction) {
        try {
            // Update customer's total spending
            String updateSpendingSql = """
                UPDATE customers 
                SET total_spending = total_spending + ?,
                    last_transaction_time = ?,
                    updated_at = ?
                WHERE customer_id = ?
                """;

            jdbcTemplate.update(updateSpendingSql,
                transaction.getAmount(),
                transaction.getTimestamp(),
                LocalDateTime.now(),
                transaction.getCustomerId()
            );

            // Check and update spending limits
            checkSpendingLimits(transaction);

        } catch (Exception e) {
            logger.error("Error updating customer data for transaction: {}", 
                        transaction.getTransactionId(), e);
        }
    }

    /**
     * Check customer spending limits
     */
    private void checkSpendingLimits(TransactionEvent transaction) {
        String sql = """
            SELECT 
                daily_limit,
                monthly_limit,
                (SELECT COALESCE(SUM(amount), 0) 
                 FROM transactions 
                 WHERE customer_id = ? 
                   AND DATE(transaction_time) = CURRENT_DATE
                   AND status = 'COMPLETED') as daily_spending,
                (SELECT COALESCE(SUM(amount), 0) 
                 FROM transactions 
                 WHERE customer_id = ? 
                   AND EXTRACT(YEAR FROM transaction_time) = EXTRACT(YEAR FROM CURRENT_DATE)
                   AND EXTRACT(MONTH FROM transaction_time) = EXTRACT(MONTH FROM CURRENT_DATE)
                   AND status = 'COMPLETED') as monthly_spending
            FROM customers 
            WHERE customer_id = ?
            """;

        try {
            Map<String, Object> limits = jdbcTemplate.queryForMap(sql, 
                transaction.getCustomerId(), 
                transaction.getCustomerId(), 
                transaction.getCustomerId()
            );

            BigDecimal dailyLimit = (BigDecimal) limits.get("daily_limit");
            BigDecimal monthlyLimit = (BigDecimal) limits.get("monthly_limit");
            BigDecimal dailySpending = (BigDecimal) limits.get("daily_spending");
            BigDecimal monthlySpending = (BigDecimal) limits.get("monthly_spending");

            // Check daily limit
            if (dailyLimit != null && dailySpending.add(transaction.getAmount()).compareTo(dailyLimit) > 0) {
                sendLimitAlert(transaction.getCustomerId(), "DAILY_LIMIT_EXCEEDED", dailySpending, dailyLimit);
            }

            // Check monthly limit
            if (monthlyLimit != null && monthlySpending.add(transaction.getAmount()).compareTo(monthlyLimit) > 0) {
                sendLimitAlert(transaction.getCustomerId(), "MONTHLY_LIMIT_EXCEEDED", monthlySpending, monthlyLimit);
            }

        } catch (Exception e) {
            logger.error("Error checking spending limits for customer: {}", 
                        transaction.getCustomerId(), e);
        }
    }

    /**
     * Send limit exceeded alert
     */
    private void sendLimitAlert(String customerId, String alertType, BigDecimal currentSpending, BigDecimal limit) {
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("customerId", customerId);
        alertData.put("currentSpending", currentSpending);
        alertData.put("limit", limit);
        alertData.put("timestamp", LocalDateTime.now());

        kafkaProducerService.sendAlert(alertType, alertData);
    }

    /**
     * Send notifications based on transaction characteristics
     */
    private void sendNotificationsIfNeeded(TransactionEvent transaction) {
        // High value transaction notification
        if (transaction.getAmount().compareTo(new BigDecimal("5000")) > 0) {
            notificationService.sendHighValueTransactionNotification(transaction);
        }

        // International transaction notification
        if (isInternationalTransaction(transaction)) {
            notificationService.sendInternationalTransactionNotification(transaction);
        }

        // First time merchant notification
        if (isFirstTimeMerchant(transaction)) {
            notificationService.sendFirstTimeMerchantNotification(transaction);
        }
    }

    /**
     * Handle fraudulent transaction
     */
    private void handleFraudulentTransaction(TransactionEvent transaction) {
        logger.warn("Fraudulent transaction detected: {}", transaction.getTransactionId());

        // Update transaction status
        updateTransactionStatus(transaction.getTransactionId(), "FRAUD_DETECTED");

        // Send fraud alert
        Map<String, Object> fraudData = new HashMap<>();
        fraudData.put("transactionId", transaction.getTransactionId());
        fraudData.put("customerId", transaction.getCustomerId());
        fraudData.put("amount", transaction.getAmount());
        fraudData.put("category", transaction.getCategory());
        fraudData.put("timestamp", LocalDateTime.now());

        kafkaProducerService.sendAlert("FRAUD_DETECTED", fraudData);

        // Block customer account temporarily
        blockCustomerAccount(transaction.getCustomerId(), "FRAUD_SUSPICION");

        // Send notification to customer
        notificationService.sendFraudAlertNotification(transaction);
    }

    /**
     * Update transaction status
     */
    private void updateTransactionStatus(String transactionId, String status) {
        String sql = "UPDATE transactions SET status = ?, updated_at = ? WHERE transaction_id = ?";
        jdbcTemplate.update(sql, status, LocalDateTime.now(), transactionId);
    }

    /**
     * Block customer account
     */
    private void blockCustomerAccount(String customerId, String reason) {
        String sql = """
            UPDATE customers 
            SET status = 'BLOCKED', 
                block_reason = ?, 
                blocked_at = ?, 
                updated_at = ? 
            WHERE customer_id = ?
            """;

        jdbcTemplate.update(sql, reason, LocalDateTime.now(), LocalDateTime.now(), customerId);
        
        logger.warn("Customer account blocked: {} for reason: {}", customerId, reason);
    }

    // Helper methods
    private boolean isInternationalTransaction(TransactionEvent transaction) {
        // Simplified check - in production, check merchant country, IP geolocation, etc.
        return transaction.getCategory().toLowerCase().contains("international") ||
               transaction.getCategory().toLowerCase().contains("foreign");
    }

    private boolean isFirstTimeMerchant(TransactionEvent transaction) {
        try {
            String sql = """
                SELECT COUNT(*) 
                FROM transactions 
                WHERE customer_id = ? 
                  AND category = ? 
                  AND status = 'COMPLETED'
                """;

            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, 
                transaction.getCustomerId(), transaction.getCategory());

            return count != null && count == 0;

        } catch (Exception e) {
            logger.error("Error checking first time merchant", e);
            return false;
        }
    }

    /**
     * Get transaction processing statistics
     */
    public Map<String, Object> getProcessingStatistics() {
        try {
            String sql = """
                SELECT 
                    status,
                    COUNT(*) as count,
                    COALESCE(SUM(amount), 0) as total_amount
                FROM transactions 
                WHERE DATE(created_at) = CURRENT_DATE
                GROUP BY status
                """;

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("dailyStatistics", results);
            statistics.put("generatedAt", LocalDateTime.now());
            
            return statistics;

        } catch (Exception e) {
            logger.error("Error getting processing statistics", e);
            return Map.of("error", "Unable to retrieve statistics");
        }
    }
}
