package com.bigdata.kafka.producer;

import com.bigdata.kafka.model.AlertEvent;
import com.bigdata.kafka.model.TransactionEvent;
import com.bigdata.kafka.model.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KafkaProducerService {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.transactions}")
    private String transactionsTopic;
    
    @Value("${kafka.topics.user-events}")
    private String userEventsTopic;
    
    @Value("${kafka.topics.alerts}")
    private String alertsTopic;
    
    @Value("${kafka.topics.analytics}")
    private String analyticsTopic;
    
    @Value("${kafka.topics.notifications}")
    private String notificationsTopic;
    
    @Value("${app.kafka.producer.send-timeout:5000}")
    private long sendTimeout;
    
    /**
     * Send transaction event with retry mechanism
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<SendResult<String, Object>> sendTransactionEvent(TransactionEvent event) {
        try {
            log.info("Sending transaction event: {} for customer: {}", 
                    event.getTransactionId(), event.getCustomerId());
            
            // Use customer ID as partition key for ordering
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(transactionsTopic, event.getCustomerId(), event);
            
            future.whenComplete((result, failure) -> {
                if (failure == null) {
                    log.info("Transaction event sent successfully: {} to partition: {} with offset: {}",
                            event.getTransactionId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send transaction event: {}", event.getTransactionId(), failure);
                }
            });
            
            return future;
        } catch (Exception e) {
            log.error("Error sending transaction event: {}", event.getTransactionId(), e);
            throw e;
        }
    }
    
    /**
     * Send user event asynchronously
     */
    @Async
    public CompletableFuture<Void> sendUserEventAsync(UserEvent event) {
        try {
            // Calculate partition based on user ID hash
            int partition = Math.abs(event.getUserId().hashCode()) % 3;
            
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(userEventsTopic, partition, event.getUserId(), event);
            
            return future.thenApply(result -> {
                log.debug("User event sent: {} for user: {} to partition: {}",
                        event.getEventType(), event.getUserId(), partition);
                return null;
            }).exceptionally(throwable -> {
                log.error("Failed to send user event: {} for user: {}", 
                        event.getEventType(), event.getUserId(), throwable);
                return null;
            });
        } catch (Exception e) {
            log.error("Error sending user event: {}", event.getEventId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Send alert event with high priority
     */
    public void sendAlertEvent(AlertEvent alert) {
        try {
            log.warn("Sending alert: {} with severity: {}", alert.getAlertType(), alert.getSeverity());
            
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(alertsTopic, alert.getAlertId(), alert);
            
            // Wait for confirmation on critical alerts
            if (alert.getSeverity() == AlertEvent.AlertSeverity.CRITICAL) {
                future.get(sendTimeout, TimeUnit.MILLISECONDS);
                log.info("Critical alert sent successfully: {}", alert.getAlertId());
            }
        } catch (Exception e) {
            log.error("Failed to send alert: {}", alert.getAlertId(), e);
            throw new RuntimeException("Alert send failed", e);
        }
    }
    
    /**
     * Send batch of user events efficiently
     */
    @Async
    public CompletableFuture<Void> sendBatchUserEvents(List<UserEvent> events) {
        log.info("Sending batch of {} user events", events.size());
        
        List<CompletableFuture<SendResult<String, Object>>> futures = events.stream()
            .map(event -> kafkaTemplate.send(userEventsTopic, event.getUserId(), event))
            .toList();
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> log.info("Batch of {} user events sent successfully", events.size()))
            .exceptionally(throwable -> {
                log.error("Failed to send batch user events", throwable);
                return null;
            });
    }
    
    /**
     * Send analytics event for real-time processing
     */
    public void sendAnalyticsEvent(String eventType, Map<String, Object> data) {
        try {
            Map<String, Object> analyticsEvent = Map.of(
                "event_id", UUID.randomUUID().toString(),
                "event_type", eventType,
                "timestamp", LocalDateTime.now(),
                "data", data
            );
            
            kafkaTemplate.send(analyticsTopic, eventType, analyticsEvent)
                .whenComplete((result, failure) -> {
                    if (failure == null) {
                        log.debug("Analytics event sent: {}", eventType);
                    } else {
                        log.error("Failed to send analytics event: {}", eventType, failure);
                    }
                });
        } catch (Exception e) {
            log.error("Error sending analytics event: {}", eventType, e);
        }
    }
    
    /**
     * Send notification event
     */
    public void sendNotificationEvent(String userId, String notificationType, Map<String, Object> data) {
        try {
            Map<String, Object> notification = Map.of(
                "notification_id", UUID.randomUUID().toString(),
                "user_id", userId,
                "type", notificationType,
                "timestamp", LocalDateTime.now(),
                "data", data,
                "priority", data.getOrDefault("priority", "normal")
            );
            
            kafkaTemplate.send(notificationsTopic, userId, notification);
            log.debug("Notification sent to user: {} type: {}", userId, notificationType);
        } catch (Exception e) {
            log.error("Error sending notification to user: {}", userId, e);
        }
    }
    
    /**
     * Send custom event to any topic
     */
    public CompletableFuture<SendResult<String, Object>> sendCustomEvent(
            String topic, String key, Object event) {
        try {
            log.debug("Sending custom event to topic: {} with key: {}", topic, key);
            return kafkaTemplate.send(topic, key, event);
        } catch (Exception e) {
            log.error("Error sending custom event to topic: {}", topic, e);
            throw e;
        }
    }
    
    /**
     * Recovery method for failed transaction sends
     */
    @Recover
    public CompletableFuture<SendResult<String, Object>> recoverTransactionSend(
            Exception ex, TransactionEvent event) {
        log.error("All retry attempts failed for transaction: {}. Sending to DLQ", 
                event.getTransactionId(), ex);
        
        // Send to dead letter queue
        String dlqTopic = transactionsTopic + "-dlq";
        return kafkaTemplate.send(dlqTopic, event.getCustomerId(), event);
    }
    
    /**
     * Send health check message
     */
    public boolean sendHealthCheck() {
        try {
            String healthCheckTopic = "health-check";
            Map<String, Object> healthMsg = Map.of(
                "timestamp", LocalDateTime.now(),
                "source", "kafka-producer-service",
                "status", "ok"
            );
            
            kafkaTemplate.send(healthCheckTopic, "health", healthMsg)
                .get(5, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.error("Health check failed", e);
            return false;
        }
    }
}
