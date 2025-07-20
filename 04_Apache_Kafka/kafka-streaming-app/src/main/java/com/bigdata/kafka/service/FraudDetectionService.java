package com.bigdata.kafka.service;

import com.bigdata.kafka.model.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Fraud Detection Service
 * Implements real-time fraud detection algorithms and rules
 */
@Service
public class FraudDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Fraud detection thresholds
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000");
    private static final int MAX_TRANSACTIONS_PER_MINUTE = 10;
    private static final int MAX_TRANSACTIONS_PER_HOUR = 50;
    private static final BigDecimal MAX_HOURLY_AMOUNT = new BigDecimal("50000");

    /**
     * Main fraud detection method
     */
    public boolean checkForFraud(TransactionEvent transaction) {
        logger.debug("Checking fraud for transaction: {}", transaction.getTransactionId());

        try {
            // 1. Check velocity rules
            if (checkVelocityFraud(transaction)) {
                logFraudDetection(transaction, "VELOCITY_FRAUD", "Too many transactions in short time");
                return true;
            }

            // 2. Check amount-based rules
            if (checkAmountFraud(transaction)) {
                logFraudDetection(transaction, "AMOUNT_FRAUD", "Suspicious transaction amount");
                return true;
            }

            // 3. Check pattern-based rules
            if (checkPatternFraud(transaction)) {
                logFraudDetection(transaction, "PATTERN_FRAUD", "Suspicious transaction pattern");
                return true;
            }

            // 4. Check geographical rules
            if (checkGeographicalFraud(transaction)) {
                logFraudDetection(transaction, "GEOGRAPHICAL_FRAUD", "Suspicious location");
                return true;
            }

            // 5. Check behavioral rules
            if (checkBehavioralFraud(transaction)) {
                logFraudDetection(transaction, "BEHAVIORAL_FRAUD", "Unusual customer behavior");
                return true;
            }

            // 6. Check merchant rules
            if (checkMerchantFraud(transaction)) {
                logFraudDetection(transaction, "MERCHANT_FRAUD", "High-risk merchant");
                return true;
            }

            logger.debug("No fraud detected for transaction: {}", transaction.getTransactionId());
            return false;

        } catch (Exception e) {
            logger.error("Error during fraud detection for transaction: {}", 
                        transaction.getTransactionId(), e);
            // In case of error, be conservative and flag as potential fraud
            return true;
        }
    }

    /**
     * Check velocity-based fraud (transaction frequency)
     */
    private boolean checkVelocityFraud(TransactionEvent transaction) {
        String customerId = transaction.getCustomerId();
        
        // Check transactions per minute
        String minuteKey = "velocity:minute:" + customerId + ":" + 
                          (System.currentTimeMillis() / 60000);
        
        Long minuteCount = redisTemplate.opsForValue().increment(minuteKey);
        redisTemplate.expire(minuteKey, 2, TimeUnit.MINUTES);
        
        if (minuteCount > MAX_TRANSACTIONS_PER_MINUTE) {
            logger.warn("Velocity fraud detected - too many transactions per minute: {} for customer: {}", 
                       minuteCount, customerId);
            return true;
        }

        // Check transactions per hour
        String hourKey = "velocity:hour:" + customerId + ":" + 
                        (System.currentTimeMillis() / 3600000);
        
        Long hourCount = redisTemplate.opsForValue().increment(hourKey);
        redisTemplate.expire(hourKey, 2, TimeUnit.HOURS);
        
        if (hourCount > MAX_TRANSACTIONS_PER_HOUR) {
            logger.warn("Velocity fraud detected - too many transactions per hour: {} for customer: {}", 
                       hourCount, customerId);
            return true;
        }

        // Check hourly amount
        String amountKey = "amount:hour:" + customerId + ":" + 
                          (System.currentTimeMillis() / 3600000);
        
        String currentAmountStr = (String) redisTemplate.opsForValue().get(amountKey);
        BigDecimal currentAmount = currentAmountStr != null ? 
                                 new BigDecimal(currentAmountStr) : BigDecimal.ZERO;
        
        BigDecimal newAmount = currentAmount.add(transaction.getAmount());
        redisTemplate.opsForValue().set(amountKey, newAmount.toString(), 2, TimeUnit.HOURS);
        
        if (newAmount.compareTo(MAX_HOURLY_AMOUNT) > 0) {
            logger.warn("Velocity fraud detected - excessive hourly amount: {} for customer: {}", 
                       newAmount, customerId);
            return true;
        }

        return false;
    }

    /**
     * Check amount-based fraud rules
     */
    private boolean checkAmountFraud(TransactionEvent transaction) {
        BigDecimal amount = transaction.getAmount();
        String customerId = transaction.getCustomerId();

        // Check if amount is suspiciously high
        if (amount.compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            // Check if customer has history of high-value transactions
            if (!hasHighValueHistory(customerId)) {
                logger.warn("Amount fraud detected - unusually high transaction: {} for customer: {}", 
                           amount, customerId);
                return true;
            }
        }

        // Check for round number fraud (common in fraud)
        if (isRoundAmount(amount) && amount.compareTo(new BigDecimal("1000")) > 0) {
            logger.debug("Potential round amount fraud detected: {} for customer: {}", 
                        amount, customerId);
            // This alone isn't enough for fraud, but increases suspicion score
        }

        // Check for micro-transactions followed by large transactions
        if (amount.compareTo(new BigDecimal("10")) <= 0) {
            String testKey = "micro:test:" + customerId;
            redisTemplate.opsForValue().set(testKey, "true", 10, TimeUnit.MINUTES);
        } else if (amount.compareTo(new BigDecimal("1000")) > 0) {
            String testKey = "micro:test:" + customerId;
            if (redisTemplate.hasKey(testKey)) {
                logger.warn("Micro-transaction test fraud detected for customer: {}", customerId);
                return true;
            }
        }

        return false;
    }

    /**
     * Check pattern-based fraud
     */
    private boolean checkPatternFraud(TransactionEvent transaction) {
        String customerId = transaction.getCustomerId();
        
        // Check for repetitive transactions (same amount, category)
        String patternKey = "pattern:" + customerId + ":" + 
                           transaction.getAmount() + ":" + transaction.getCategory();
        
        Long patternCount = redisTemplate.opsForValue().increment(patternKey);
        redisTemplate.expire(patternKey, 1, TimeUnit.HOURS);
        
        if (patternCount > 5) {
            logger.warn("Pattern fraud detected - repetitive transactions for customer: {}", customerId);
            return true;
        }

        // Check for unusual time patterns
        int hour = transaction.getTimestamp().getHour();
        if ((hour < 6 || hour > 23) && transaction.getAmount().compareTo(new BigDecimal("1000")) > 0) {
            if (!hasNightTimeHistory(customerId)) {
                logger.warn("Pattern fraud detected - unusual time transaction for customer: {}", customerId);
                return true;
            }
        }

        return false;
    }

    /**
     * Check geographical fraud
     */
    private boolean checkGeographicalFraud(TransactionEvent transaction) {
        // In a real implementation, you would check:
        // - IP geolocation vs customer's registered address
        // - Multiple locations in short time span
        // - High-risk countries
        
        String customerId = transaction.getCustomerId();
        String category = transaction.getCategory().toLowerCase();
        
        // Simple check for international transactions
        if (category.contains("international") || category.contains("foreign")) {
            if (!hasInternationalHistory(customerId)) {
                logger.warn("Geographical fraud detected - first international transaction for customer: {}", 
                           customerId);
                return true;
            }
        }

        return false;
    }

    /**
     * Check behavioral fraud
     */
    private boolean checkBehavioralFraud(TransactionEvent transaction) {
        String customerId = transaction.getCustomerId();
        
        try {
            // Get customer's typical transaction behavior
            String sql = """
                SELECT 
                    AVG(amount) as avg_amount,
                    STDDEV(amount) as stddev_amount,
                    COUNT(*) as transaction_count
                FROM transactions 
                WHERE customer_id = ? 
                  AND status = 'COMPLETED'
                  AND transaction_time >= NOW() - INTERVAL '30 days'
                """;

            Map<String, Object> behavior = jdbcTemplate.queryForMap(sql, customerId);
            
            if (behavior.get("transaction_count") != null && 
                ((Number) behavior.get("transaction_count")).intValue() > 10) {
                
                BigDecimal avgAmount = (BigDecimal) behavior.get("avg_amount");
                BigDecimal stddevAmount = (BigDecimal) behavior.get("stddev_amount");
                
                if (avgAmount != null && stddevAmount != null) {
                    // Check if current transaction is more than 3 standard deviations from average
                    BigDecimal threshold = avgAmount.add(stddevAmount.multiply(new BigDecimal("3")));
                    
                    if (transaction.getAmount().compareTo(threshold) > 0) {
                        logger.warn("Behavioral fraud detected - transaction {} significantly above normal for customer: {}", 
                                   transaction.getAmount(), customerId);
                        return true;
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error checking behavioral fraud for customer: {}", customerId, e);
        }

        return false;
    }

    /**
     * Check merchant-based fraud
     */
    private boolean checkMerchantFraud(TransactionEvent transaction) {
        String category = transaction.getCategory().toLowerCase();
        
        // High-risk categories
        if (category.contains("gambling") || 
            category.contains("cryptocurrency") || 
            category.contains("cash advance") ||
            category.contains("money transfer")) {
            
            logger.debug("High-risk merchant category detected: {}", category);
            
            // Additional checks for high-risk merchants
            if (transaction.getAmount().compareTo(new BigDecimal("5000")) > 0) {
                logger.warn("Merchant fraud detected - high amount with high-risk category: {}", category);
                return true;
            }
        }

        return false;
    }

    // Helper methods
    private boolean hasHighValueHistory(String customerId) {
        try {
            String sql = """
                SELECT COUNT(*) 
                FROM transactions 
                WHERE customer_id = ? 
                  AND amount > ? 
                  AND status = 'COMPLETED'
                  AND transaction_time >= NOW() - INTERVAL '90 days'
                """;

            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, 
                customerId, HIGH_VALUE_THRESHOLD);

            return count != null && count > 0;

        } catch (Exception e) {
            logger.error("Error checking high value history for customer: {}", customerId, e);
            return false;
        }
    }

    private boolean isRoundAmount(BigDecimal amount) {
        return amount.remainder(new BigDecimal("100")).equals(BigDecimal.ZERO) ||
               amount.remainder(new BigDecimal("1000")).equals(BigDecimal.ZERO);
    }

    private boolean hasNightTimeHistory(String customerId) {
        try {
            String sql = """
                SELECT COUNT(*) 
                FROM transactions 
                WHERE customer_id = ? 
                  AND (EXTRACT(HOUR FROM transaction_time) < 6 OR EXTRACT(HOUR FROM transaction_time) > 23)
                  AND status = 'COMPLETED'
                  AND transaction_time >= NOW() - INTERVAL '30 days'
                """;

            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, customerId);

            return count != null && count > 5;

        } catch (Exception e) {
            logger.error("Error checking night time history for customer: {}", customerId, e);
            return false;
        }
    }

    private boolean hasInternationalHistory(String customerId) {
        try {
            String sql = """
                SELECT COUNT(*) 
                FROM transactions 
                WHERE customer_id = ? 
                  AND (LOWER(category) LIKE '%international%' OR LOWER(category) LIKE '%foreign%')
                  AND status = 'COMPLETED'
                  AND transaction_time >= NOW() - INTERVAL '90 days'
                """;

            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, customerId);

            return count != null && count > 0;

        } catch (Exception e) {
            logger.error("Error checking international history for customer: {}", customerId, e);
            return false;
        }
    }

    private void logFraudDetection(TransactionEvent transaction, String fraudType, String reason) {
        try {
            String sql = """
                INSERT INTO fraud_detections (
                    transaction_id, customer_id, fraud_type, reason, 
                    amount, category, detected_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

            jdbcTemplate.update(sql,
                transaction.getTransactionId(),
                transaction.getCustomerId(),
                fraudType,
                reason,
                transaction.getAmount(),
                transaction.getCategory(),
                LocalDateTime.now()
            );

            logger.info("Fraud detection logged: {} for transaction: {}", 
                       fraudType, transaction.getTransactionId());

        } catch (Exception e) {
            logger.error("Error logging fraud detection", e);
        }
    }

    /**
     * Get fraud detection statistics
     */
    public Map<String, Object> getFraudStatistics() {
        try {
            String sql = """
                SELECT 
                    fraud_type,
                    COUNT(*) as detection_count,
                    DATE(detected_at) as detection_date
                FROM fraud_detections 
                WHERE detected_at >= NOW() - INTERVAL '7 days'
                GROUP BY fraud_type, DATE(detected_at)
                ORDER BY detection_date DESC, detection_count DESC
                """;

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("fraudStatistics", results);
            statistics.put("generatedAt", LocalDateTime.now());
            
            return statistics;

        } catch (Exception e) {
            logger.error("Error getting fraud statistics", e);
            return Map.of("error", "Unable to retrieve fraud statistics");
        }
    }
}
