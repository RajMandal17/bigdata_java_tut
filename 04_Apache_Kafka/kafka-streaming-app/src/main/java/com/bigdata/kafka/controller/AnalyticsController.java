package com.bigdata.kafka.controller;

import com.bigdata.kafka.streams.InteractiveQueriesService;
import com.bigdata.kafka.service.TransactionProcessingService;
import com.bigdata.kafka.service.FraudDetectionService;
import com.bigdata.kafka.error.DeadLetterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analytics and Monitoring Controller
 * REST API endpoints for analytics, monitoring, and management
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    private InteractiveQueriesService interactiveQueriesService;

    @Autowired
    private TransactionProcessingService transactionProcessingService;

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private DeadLetterService deadLetterService;

    /**
     * Get customer spending analytics
     */
    @GetMapping("/customer/{customerId}/spending")
    public ResponseEntity<Map<String, Object>> getCustomerSpending(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "1") int hours) {
        
        logger.info("Getting customer spending for: {} over {} hours", customerId, hours);

        try {
            Duration duration = Duration.ofHours(hours);
            Map<String, Object> spendingData = interactiveQueriesService.getCustomerSpending(customerId, duration);
            
            spendingData.put("requestedDuration", hours + " hours");
            spendingData.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(spendingData);

        } catch (Exception e) {
            logger.error("Error getting customer spending", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Unable to retrieve customer spending data");
            response.put("customerId", customerId);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get customer transaction counts
     */
    @GetMapping("/customer/{customerId}/transactions")
    public ResponseEntity<Map<String, Object>> getCustomerTransactionCounts(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "24") int hours) {
        
        logger.info("Getting transaction counts for customer: {} over {} hours", customerId, hours);

        try {
            Duration duration = Duration.ofHours(hours);
            Map<String, Object> transactionData = interactiveQueriesService.getCustomerTransactionCounts(customerId, duration);
            
            transactionData.put("requestedDuration", hours + " hours");
            transactionData.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(transactionData);

        } catch (Exception e) {
            logger.error("Error getting customer transaction counts", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Unable to retrieve transaction count data");
            response.put("customerId", customerId);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get top spenders
     */
    @GetMapping("/top-spenders")
    public ResponseEntity<List<Map<String, Object>>> getTopSpenders(
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.info("Getting top {} spenders", limit);

        try {
            List<Map<String, Object>> topSpenders = interactiveQueriesService.getTopSpenders(limit);
            return ResponseEntity.ok(topSpenders);

        } catch (Exception e) {
            logger.error("Error getting top spenders", e);
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    /**
     * Get analytics dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAnalyticsDashboard() {
        logger.info("Getting analytics dashboard data");

        try {
            Map<String, Object> dashboard = interactiveQueriesService.getAnalyticsDashboard();
            
            // Add additional dashboard metrics
            dashboard.put("transactionStatistics", transactionProcessingService.getProcessingStatistics());
            dashboard.put("fraudStatistics", fraudDetectionService.getFraudStatistics());
            dashboard.put("retryStatistics", deadLetterService.getRetryStatistics());

            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            logger.error("Error getting analytics dashboard", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Unable to retrieve dashboard data");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get fraud detection statistics
     */
    @GetMapping("/fraud/statistics")
    public ResponseEntity<Map<String, Object>> getFraudStatistics() {
        logger.info("Getting fraud detection statistics");

        try {
            Map<String, Object> fraudStats = fraudDetectionService.getFraudStatistics();
            return ResponseEntity.ok(fraudStats);

        } catch (Exception e) {
            logger.error("Error getting fraud statistics", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Unable to retrieve fraud statistics");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get transaction processing statistics
     */
    @GetMapping("/transactions/statistics")
    public ResponseEntity<Map<String, Object>> getTransactionStatistics() {
        logger.info("Getting transaction processing statistics");

        try {
            Map<String, Object> transactionStats = transactionProcessingService.getProcessingStatistics();
            return ResponseEntity.ok(transactionStats);

        } catch (Exception e) {
            logger.error("Error getting transaction statistics", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Unable to retrieve transaction statistics");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get failed messages for manual review
     */
    @GetMapping("/failed-messages")
    public ResponseEntity<List<Map<String, Object>>> getFailedMessages(
            @RequestParam(defaultValue = "50") int limit) {
        
        logger.info("Getting failed messages (limit: {})", limit);

        try {
            List<Map<String, Object>> failedMessages = deadLetterService.getFailedMessages(limit);
            return ResponseEntity.ok(failedMessages);

        } catch (Exception e) {
            logger.error("Error getting failed messages", e);
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    /**
     * Mark failed message as resolved
     */
    @PutMapping("/failed-messages/{messageId}/resolve")
    public ResponseEntity<Map<String, Object>> markFailedMessageAsResolved(
            @PathVariable Long messageId) {
        
        logger.info("Marking failed message as resolved: {}", messageId);

        try {
            deadLetterService.markAsResolved(messageId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messageId", messageId);
            response.put("message", "Failed message marked as resolved");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error marking failed message as resolved", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("messageId", messageId);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get retry statistics
     */
    @GetMapping("/retry/statistics")
    public ResponseEntity<Map<String, Object>> getRetryStatistics() {
        logger.info("Getting retry statistics");

        try {
            Map<String, Object> retryStats = deadLetterService.getRetryStatistics();
            return ResponseEntity.ok(retryStats);

        } catch (Exception e) {
            logger.error("Error getting retry statistics", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Unable to retrieve retry statistics");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Check system health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        logger.info("Checking system health");

        try {
            Map<String, Object> health = new HashMap<>();
            
            // Check Kafka Streams health
            boolean streamsReady = interactiveQueriesService.isReady();
            health.put("kafkaStreams", streamsReady ? "UP" : "DOWN");
            
            // System timestamp
            health.put("timestamp", LocalDateTime.now());
            health.put("status", streamsReady ? "UP" : "DOWN");

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            logger.error("Error checking system health", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Get real-time metrics
     */
    @GetMapping("/metrics/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeMetrics() {
        logger.info("Getting real-time metrics");

        try {
            Map<String, Object> metrics = new HashMap<>();
            
            // Get current top spenders
            metrics.put("topSpenders", interactiveQueriesService.getTopSpenders(5));
            
            // Get current system health
            metrics.put("systemHealth", getSystemHealth().getBody());
            
            // Add timestamp
            metrics.put("timestamp", LocalDateTime.now());
            metrics.put("refreshInterval", "30 seconds");

            return ResponseEntity.ok(metrics);

        } catch (Exception e) {
            logger.error("Error getting real-time metrics", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Unable to retrieve real-time metrics");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
