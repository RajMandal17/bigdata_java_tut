package com.bigdata.foundations.kafka;

import com.bigdata.foundations.mongodb.document.Event;
import com.bigdata.foundations.mongodb.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class EventStreamProcessor {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Kafka Consumers
    @KafkaListener(topics = "user-events", groupId = "database-foundations-group")
    public void handleUserEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        try {
            System.out.printf("Received user event from topic: %s, partition: %d, offset: %d%n", 
                topic, partition, offset);
            
            // Parse the JSON message
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            
            // Create MongoDB event document
            Event event = new Event();
            event.setEventId("kafka_" + System.currentTimeMillis());
            event.setUserId((String) eventData.get("userId"));
            event.setEventType((String) eventData.get("action"));
            event.setSource("kafka-stream");
            event.setTimestamp(LocalDateTime.now());
            
            // Set event data
            Map<String, Object> data = new HashMap<>();
            data.putAll(eventData);
            event.setData(data);
            
            // Save to MongoDB
            eventRepository.save(event);
            
            System.out.println("User event processed and saved: " + event.getEventId());
            
        } catch (Exception e) {
            System.err.println("Error processing user event: " + e.getMessage());
            // Send to dead letter queue or error topic
            sendToErrorTopic(message, "user-events", e.getMessage());
        }
    }
    
    @KafkaListener(topics = "order-events", groupId = "database-foundations-group")
    public void handleOrderEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        try {
            System.out.println("Processing order event from topic: " + topic);
            
            Map<String, Object> orderData = objectMapper.readValue(message, Map.class);
            
            Event event = new Event();
            event.setEventId("order_" + System.currentTimeMillis());
            event.setUserId((String) orderData.get("userId"));
            event.setEventType("order_" + orderData.get("status"));
            event.setSource("order-system");
            event.setTimestamp(LocalDateTime.now());
            
            Map<String, Object> data = new HashMap<>();
            data.putAll(orderData);
            event.setData(data);
            
            eventRepository.save(event);
            
            // If order is confirmed, trigger analytics
            if ("confirmed".equals(orderData.get("status"))) {
                triggerOrderAnalytics(orderData);
            }
            
            System.out.println("Order event processed: " + event.getEventId());
            
        } catch (Exception e) {
            System.err.println("Error processing order event: " + e.getMessage());
            sendToErrorTopic(message, "order-events", e.getMessage());
        }
    }
    
    @KafkaListener(topics = "system-metrics", groupId = "database-foundations-group")
    public void handleSystemMetrics(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        try {
            System.out.println("Processing system metrics from topic: " + topic);
            
            Map<String, Object> metricsData = objectMapper.readValue(message, Map.class);
            
            Event event = new Event();
            event.setEventId("metrics_" + System.currentTimeMillis());
            event.setEventType("system_metrics");
            event.setSource((String) metricsData.get("service"));
            event.setTimestamp(LocalDateTime.now());
            
            event.setData(metricsData);
            eventRepository.save(event);
            
            // Check for alerts
            checkSystemAlerts(metricsData);
            
        } catch (Exception e) {
            System.err.println("Error processing system metrics: " + e.getMessage());
        }
    }
    
    // Kafka Producers
    public void publishUserActivity(String userId, String action, Map<String, Object> data) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("userId", userId);
            event.put("action", action);
            event.put("timestamp", LocalDateTime.now().toString());
            event.putAll(data);
            
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("user-events", userId, message);
            
            System.out.println("Published user activity: " + action + " for user: " + userId);
            
        } catch (Exception e) {
            System.err.println("Error publishing user activity: " + e.getMessage());
        }
    }
    
    public void publishOrderEvent(String orderId, String userId, String status, Map<String, Object> orderData) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("orderId", orderId);
            event.put("userId", userId);
            event.put("status", status);
            event.put("timestamp", LocalDateTime.now().toString());
            event.putAll(orderData);
            
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("order-events", orderId, message);
            
            System.out.println("Published order event: " + status + " for order: " + orderId);
            
        } catch (Exception e) {
            System.err.println("Error publishing order event: " + e.getMessage());
        }
    }
    
    public void publishSystemMetrics(String service, Map<String, Object> metrics) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("service", service);
            event.put("timestamp", LocalDateTime.now().toString());
            event.putAll(metrics);
            
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("system-metrics", service, message);
            
        } catch (Exception e) {
            System.err.println("Error publishing system metrics: " + e.getMessage());
        }
    }
    
    // Helper methods
    private void sendToErrorTopic(String originalMessage, String sourceTopic, String error) {
        try {
            Map<String, Object> errorEvent = new HashMap<>();
            errorEvent.put("originalMessage", originalMessage);
            errorEvent.put("sourceTopic", sourceTopic);
            errorEvent.put("error", error);
            errorEvent.put("timestamp", LocalDateTime.now().toString());
            
            String errorMessage = objectMapper.writeValueAsString(errorEvent);
            kafkaTemplate.send("error-logs", "error", errorMessage);
            
        } catch (Exception e) {
            System.err.println("Error sending to error topic: " + e.getMessage());
        }
    }
    
    private void triggerOrderAnalytics(Map<String, Object> orderData) {
        // Example: Calculate revenue, update customer metrics, etc.
        System.out.println("Triggering analytics for confirmed order: " + orderData.get("orderId"));
        
        // You could send this to another topic for analytics processing
        kafkaTemplate.send("analytics-events", "order-analytics", 
            "Order confirmed: " + orderData.get("orderId") + ", Amount: " + orderData.get("amount"));
    }
    
    private void checkSystemAlerts(Map<String, Object> metricsData) {
        // Example: Check for high CPU or memory usage
        Object cpuObj = metricsData.get("cpu");
        Object memoryObj = metricsData.get("memory");
        
        if (cpuObj instanceof Number && ((Number) cpuObj).doubleValue() > 80.0) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "high_cpu");
            alert.put("service", metricsData.get("service"));
            alert.put("value", cpuObj);
            alert.put("threshold", 80.0);
            alert.put("timestamp", LocalDateTime.now().toString());
            
            try {
                String alertMessage = objectMapper.writeValueAsString(alert);
                kafkaTemplate.send("system-alerts", "cpu-alert", alertMessage);
            } catch (Exception e) {
                System.err.println("Error sending CPU alert: " + e.getMessage());
            }
        }
        
        if (memoryObj instanceof Number && ((Number) memoryObj).doubleValue() > 85.0) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "high_memory");
            alert.put("service", metricsData.get("service"));
            alert.put("value", memoryObj);
            alert.put("threshold", 85.0);
            alert.put("timestamp", LocalDateTime.now().toString());
            
            try {
                String alertMessage = objectMapper.writeValueAsString(alert);
                kafkaTemplate.send("system-alerts", "memory-alert", alertMessage);
            } catch (Exception e) {
                System.err.println("Error sending memory alert: " + e.getMessage());
            }
        }
    }
}
