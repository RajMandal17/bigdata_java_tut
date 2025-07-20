package com.bigdata.kafka.controller;

import com.bigdata.kafka.model.TransactionEvent;
import com.bigdata.kafka.model.UserEvent;
import com.bigdata.kafka.producer.KafkaProducerService;
import com.bigdata.kafka.service.TransactionProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Event Producer Controller
 * REST API endpoints for producing events to Kafka topics
 */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventProducerController {

    private static final Logger logger = LoggerFactory.getLogger(EventProducerController.class);

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private TransactionProcessingService transactionProcessingService;

    /**
     * Send a single transaction event
     */
    @PostMapping("/transaction")
    public ResponseEntity<Map<String, Object>> sendTransactionEvent(
            @Valid @RequestBody TransactionEventRequest request) {
        
        logger.info("Received transaction event request for customer: {}", request.getCustomerId());

        try {
            TransactionEvent event = TransactionEvent.builder()
                .transactionId(UUID.randomUUID().toString())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .category(request.getCategory())
                .timestamp(LocalDateTime.now())
                .status("PENDING")
                .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                .build();

            kafkaProducerService.sendTransactionEvent(event);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactionId", event.getTransactionId());
            response.put("message", "Transaction event sent successfully");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error sending transaction event", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Send batch of transaction events
     */
    @PostMapping("/transaction/batch")
    public ResponseEntity<Map<String, Object>> sendBatchTransactionEvents(
            @Valid @RequestBody List<TransactionEventRequest> requests) {
        
        logger.info("Received batch transaction request with {} events", requests.size());

        try {
            int successCount = 0;
            int failureCount = 0;
            Map<String, String> results = new HashMap<>();

            for (TransactionEventRequest request : requests) {
                try {
                    TransactionEvent event = TransactionEvent.builder()
                        .transactionId(UUID.randomUUID().toString())
                        .customerId(request.getCustomerId())
                        .amount(request.getAmount())
                        .category(request.getCategory())
                        .timestamp(LocalDateTime.now())
                        .status("PENDING")
                        .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                        .build();

                    kafkaProducerService.sendTransactionEvent(event);
                    results.put(event.getTransactionId(), "SUCCESS");
                    successCount++;

                } catch (Exception e) {
                    logger.error("Error sending transaction event in batch", e);
                    results.put("FAILED_" + failureCount, e.getMessage());
                    failureCount++;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("totalRequests", requests.size());
            response.put("successCount", successCount);
            response.put("failureCount", failureCount);
            response.put("results", results);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing batch transaction events", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Send user event
     */
    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> sendUserEvent(
            @Valid @RequestBody UserEventRequest request) {
        
        logger.info("Received user event request for user: {}", request.getUserId());

        try {
            UserEvent event = UserEvent.builder()
                .userId(request.getUserId())
                .eventType(request.getEventType())
                .timestamp(LocalDateTime.now())
                .sessionId(request.getSessionId())
                .data(request.getData() != null ? request.getData() : new HashMap<>())
                .build();

            kafkaProducerService.sendUserEvent(event);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("eventId", UUID.randomUUID().toString());
            response.put("message", "User event sent successfully");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error sending user event", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Send alert event
     */
    @PostMapping("/alert")
    public ResponseEntity<Map<String, Object>> sendAlert(
            @Valid @RequestBody AlertRequest request) {
        
        logger.info("Received alert request: {}", request.getAlertType());

        try {
            kafkaProducerService.sendAlert(request.getAlertType(), request.getData());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("alertId", UUID.randomUUID().toString());
            response.put("message", "Alert sent successfully");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error sending alert", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Send custom event to specific topic
     */
    @PostMapping("/custom/{topic}")
    public ResponseEntity<Map<String, Object>> sendCustomEvent(
            @PathVariable String topic,
            @RequestBody Map<String, Object> eventData,
            @RequestParam(required = false) String key) {
        
        logger.info("Received custom event request for topic: {}", topic);

        try {
            String eventKey = key != null ? key : UUID.randomUUID().toString();
            kafkaProducerService.sendCustomEvent(topic, eventKey, eventData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("topic", topic);
            response.put("key", eventKey);
            response.put("message", "Custom event sent successfully");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error sending custom event to topic: {}", topic, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Generate sample data for testing
     */
    @PostMapping("/generate/sample")
    public ResponseEntity<Map<String, Object>> generateSampleData(
            @RequestParam(defaultValue = "10") int transactionCount,
            @RequestParam(defaultValue = "5") int userEventCount) {
        
        logger.info("Generating {} transactions and {} user events", transactionCount, userEventCount);

        try {
            // Generate sample transactions
            for (int i = 0; i < transactionCount; i++) {
                TransactionEvent transaction = generateSampleTransaction();
                kafkaProducerService.sendTransactionEvent(transaction);
            }

            // Generate sample user events
            for (int i = 0; i < userEventCount; i++) {
                UserEvent userEvent = generateSampleUserEvent();
                kafkaProducerService.sendUserEvent(userEvent);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactionsGenerated", transactionCount);
            response.put("userEventsGenerated", userEventCount);
            response.put("message", "Sample data generated successfully");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error generating sample data", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get producer health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getProducerHealth() {
        try {
            boolean isHealthy = kafkaProducerService.isHealthy();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", isHealthy ? "UP" : "DOWN");
            response.put("kafkaProducer", isHealthy ? "HEALTHY" : "UNHEALTHY");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error checking producer health", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(503).body(response);
        }
    }

    // Helper methods for generating sample data
    private TransactionEvent generateSampleTransaction() {
        String[] customers = {"CUST001", "CUST002", "CUST003", "CUST004", "CUST005"};
        String[] categories = {"GROCERY", "RESTAURANT", "GAS_STATION", "ONLINE_SHOPPING", "ATM", "PHARMACY"};
        
        return TransactionEvent.builder()
            .transactionId(UUID.randomUUID().toString())
            .customerId(customers[(int) (Math.random() * customers.length)])
            .amount(BigDecimal.valueOf(Math.random() * 1000 + 10))
            .category(categories[(int) (Math.random() * categories.length)])
            .timestamp(LocalDateTime.now())
            .status("PENDING")
            .metadata(Map.of("generated", true, "timestamp", System.currentTimeMillis()))
            .build();
    }

    private UserEvent generateSampleUserEvent() {
        String[] users = {"USER001", "USER002", "USER003", "USER004", "USER005"};
        String[] eventTypes = {"LOGIN", "LOGOUT", "PROFILE_VIEW", "SETTINGS_UPDATE", "PURCHASE"};
        
        return UserEvent.builder()
            .userId(users[(int) (Math.random() * users.length)])
            .eventType(eventTypes[(int) (Math.random() * eventTypes.length)])
            .timestamp(LocalDateTime.now())
            .sessionId(UUID.randomUUID().toString())
            .data(Map.of("generated", true, "timestamp", System.currentTimeMillis()))
            .build();
    }

    // Request DTOs
    public static class TransactionEventRequest {
        private String customerId;
        private BigDecimal amount;
        private String category;
        private Map<String, Object> metadata;

        // Getters and setters
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    public static class UserEventRequest {
        private String userId;
        private String eventType;
        private String sessionId;
        private Map<String, Object> data;

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }

    public static class AlertRequest {
        private String alertType;
        private Map<String, Object> data;

        // Getters and setters
        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }
}
