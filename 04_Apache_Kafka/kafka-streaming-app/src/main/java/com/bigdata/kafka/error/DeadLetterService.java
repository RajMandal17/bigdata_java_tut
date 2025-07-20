package com.bigdata.kafka.error;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Dead Letter Service for handling failed messages
 * Provides persistence and retry mechanisms for failed message processing
 */
@Service
public class DeadLetterService {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Save failed message to database for manual processing
     */
    public void saveFailedMessage(ConsumerRecord<?, ?> record, Exception exception) {
        try {
            String sql = """
                INSERT INTO failed_messages (
                    topic, partition_id, offset_value, message_key, message_value,
                    error_message, error_type, created_at, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

            jdbcTemplate.update(sql,
                record.topic(),
                record.partition(),
                record.offset(),
                record.key() != null ? record.key().toString() : null,
                record.value() != null ? record.value().toString() : null,
                exception.getMessage(),
                exception.getClass().getSimpleName(),
                LocalDateTime.now(),
                "FAILED"
            );

            logger.info("Saved failed message to database: topic={}, partition={}, offset={}",
                       record.topic(), record.partition(), record.offset());

        } catch (Exception e) {
            logger.error("Failed to save failed message to database", e);
        }
    }

    /**
     * Save failed message with custom data
     */
    public void saveFailedMessage(String topic, Object key, Object value, String errorMessage) {
        try {
            String sql = """
                INSERT INTO failed_messages (
                    topic, message_key, message_value, error_message, 
                    error_type, created_at, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

            jdbcTemplate.update(sql,
                topic,
                key != null ? key.toString() : null,
                value != null ? value.toString() : null,
                errorMessage,
                "CUSTOM_ERROR",
                LocalDateTime.now(),
                "FAILED"
            );

            logger.info("Saved custom failed message to database: topic={}", topic);

        } catch (Exception e) {
            logger.error("Failed to save custom failed message to database", e);
        }
    }

    /**
     * Log retry attempt
     */
    public void logRetryAttempt(Map<String, Object> retryData) {
        try {
            String sql = """
                INSERT INTO retry_attempts (
                    topic, partition_id, offset_value, message_key,
                    error_type, error_message, attempt_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

            jdbcTemplate.update(sql,
                retryData.get("topic"),
                retryData.get("partition"),
                retryData.get("offset"),
                retryData.get("key"),
                retryData.get("errorType"),
                retryData.get("errorMessage"),
                retryData.get("timestamp")
            );

            logger.debug("Logged retry attempt for topic: {}", retryData.get("topic"));

        } catch (Exception e) {
            logger.error("Failed to log retry attempt", e);
        }
    }

    /**
     * Get failed messages for manual review
     */
    public java.util.List<Map<String, Object>> getFailedMessages(int limit) {
        try {
            String sql = """
                SELECT id, topic, partition_id, offset_value, message_key, 
                       message_value, error_message, error_type, created_at, status
                FROM failed_messages 
                WHERE status = 'FAILED'
                ORDER BY created_at DESC 
                LIMIT ?
                """;

            return jdbcTemplate.queryForList(sql, limit);

        } catch (Exception e) {
            logger.error("Failed to retrieve failed messages", e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Mark failed message as resolved
     */
    public void markAsResolved(Long messageId) {
        try {
            String sql = "UPDATE failed_messages SET status = 'RESOLVED', resolved_at = ? WHERE id = ?";
            jdbcTemplate.update(sql, LocalDateTime.now(), messageId);
            
            logger.info("Marked failed message as resolved: id={}", messageId);

        } catch (Exception e) {
            logger.error("Failed to mark message as resolved: id={}", messageId, e);
        }
    }

    /**
     * Get retry statistics
     */
    public Map<String, Object> getRetryStatistics() {
        try {
            String sql = """
                SELECT 
                    topic,
                    COUNT(*) as retry_count,
                    MAX(attempt_time) as last_retry
                FROM retry_attempts 
                WHERE attempt_time >= ? 
                GROUP BY topic
                ORDER BY retry_count DESC
                """;

            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            var results = jdbcTemplate.queryForList(sql, yesterday);
            
            Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("retryStatistics", results);
            statistics.put("generatedAt", LocalDateTime.now());
            
            return statistics;

        } catch (Exception e) {
            logger.error("Failed to get retry statistics", e);
            return Map.of("error", "Unable to retrieve retry statistics");
        }
    }
}
