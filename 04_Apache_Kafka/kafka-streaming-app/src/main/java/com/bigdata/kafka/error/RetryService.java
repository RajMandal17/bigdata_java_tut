package com.bigdata.kafka.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Retry Service for handling message retry logic
 * Implements exponential backoff and retry strategies
 */
@Service
public class RetryService {

    private static final Logger logger = LoggerFactory.getLogger(RetryService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private DeadLetterService deadLetterService;

    /**
     * Retry sending original data with exponential backoff
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public boolean retryOriginalData(String topic, Object key, Object value) {
        try {
            logger.info("Retrying original data for topic: {}, key: {}", topic, key);
            
            kafkaTemplate.send(topic, key, value).get(10, TimeUnit.SECONDS);
            
            logger.info("Successfully retried message for topic: {}", topic);
            return true;

        } catch (Exception e) {
            logger.error("Retry failed for topic: {}, key: {}", topic, key, e);
            throw new RuntimeException("Retry failed", e);
        }
    }

    /**
     * Retry sending fixed/corrected data
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public boolean retryWithFixedData(String topic, Object key, Object fixedValue) {
        try {
            logger.info("Retrying with fixed data for topic: {}, key: {}", topic, key);
            
            kafkaTemplate.send(topic, key, fixedValue).get(10, TimeUnit.SECONDS);
            
            logger.info("Successfully retried fixed message for topic: {}", topic);
            return true;

        } catch (Exception e) {
            logger.error("Retry with fixed data failed for topic: {}, key: {}", topic, key, e);
            throw new RuntimeException("Retry with fixed data failed", e);
        }
    }

    /**
     * Recovery method called when all retries fail
     */
    @Recover
    public boolean recover(Exception e, String topic, Object key, Object value) {
        logger.error("All retry attempts failed for topic: {}, key: {}", topic, key);
        
        // Save to database as final fallback
        deadLetterService.saveFailedMessage(topic, key, value, 
            "Retry service exhausted all attempts: " + e.getMessage());
        
        return false;
    }

    /**
     * Recovery method for fixed data retries
     */
    @Recover
    public boolean recoverFixed(Exception e, String topic, Object key, Object fixedValue) {
        logger.error("All retry attempts failed for fixed data in topic: {}, key: {}", topic, key);
        
        deadLetterService.saveFailedMessage(topic, key, fixedValue, 
            "Fixed data retry exhausted all attempts: " + e.getMessage());
        
        return false;
    }
}
