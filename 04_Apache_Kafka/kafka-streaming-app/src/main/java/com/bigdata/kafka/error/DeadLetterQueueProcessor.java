package com.bigdata.kafka.error;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Dead Letter Queue Processor for handling messages that failed processing
 * Implements retry strategies and manual intervention workflows
 */
@Component
public class DeadLetterQueueProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterQueueProcessor.class);

    @Autowired
    private DeadLetterService deadLetterService;

    @Autowired
    private RetryService retryService;

    /**
     * Process failed transaction messages
     */
    @KafkaListener(topics = "transactions-topic-dlt", groupId = "dlt-processors")
    public void processFailedTransactionMessages(
            @Payload Map<String, Object> deadLetterData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Headers headers,
            Acknowledgment acknowledgment) {

        logger.warn("Processing dead letter transaction message from topic: {}, partition: {}, offset: {}",
                   topic, partition, offset);

        try {
            // Extract original message information
            String originalTopic = (String) deadLetterData.get("originalTopic");
            String errorType = (String) deadLetterData.get("errorType");
            String errorMessage = (String) deadLetterData.get("errorMessage");
            Object originalValue = deadLetterData.get("originalValue");

            logger.info("Original topic: {}, Error type: {}, Error: {}", 
                       originalTopic, errorType, errorMessage);

            // Attempt different recovery strategies based on error type
            boolean recovered = false;

            if ("JsonProcessingException".equals(errorType)) {
                recovered = handleJsonProcessingError(deadLetterData);
            } else if ("ValidationException".equals(errorType)) {
                recovered = handleValidationError(deadLetterData);
            } else if ("SQLException".equals(errorType)) {
                recovered = handleDatabaseError(deadLetterData);
            } else {
                recovered = handleGenericError(deadLetterData);
            }

            if (recovered) {
                logger.info("Successfully recovered dead letter message");
                acknowledgment.acknowledge();
            } else {
                logger.warn("Unable to recover dead letter message, saving for manual intervention");
                saveForManualIntervention(deadLetterData);
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            logger.error("Error processing dead letter message", e);
            // Don't acknowledge - let it retry
        }
    }

    /**
     * Process failed user event messages
     */
    @KafkaListener(topics = "user-events-topic-dlt", groupId = "dlt-processors")
    public void processFailedUserEventMessages(
            @Payload Map<String, Object> deadLetterData,
            Acknowledgment acknowledgment) {

        logger.warn("Processing dead letter user event message");

        try {
            // Simplified recovery for user events
            boolean recovered = attemptUserEventRecovery(deadLetterData);
            
            if (recovered) {
                logger.info("Successfully recovered dead letter user event");
            } else {
                logger.warn("Unable to recover user event, logging for analysis");
                logUserEventFailure(deadLetterData);
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            logger.error("Error processing dead letter user event", e);
        }
    }

    /**
     * Process failed alert messages
     */
    @KafkaListener(topics = "alerts-topic-dlt", groupId = "dlt-processors")
    public void processFailedAlertMessages(
            @Payload Map<String, Object> deadLetterData,
            Acknowledgment acknowledgment) {

        logger.warn("Processing dead letter alert message");

        try {
            // Alerts are critical - always attempt recovery
            boolean recovered = attemptAlertRecovery(deadLetterData);
            
            if (!recovered) {
                // If we can't recover an alert, send notification to operations team
                sendOperationalAlert(deadLetterData);
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            logger.error("Error processing dead letter alert", e);
        }
    }

    // Recovery strategies

    private boolean handleJsonProcessingError(Map<String, Object> deadLetterData) {
        try {
            // Attempt to fix common JSON issues
            Object originalValue = deadLetterData.get("originalValue");
            if (originalValue instanceof String) {
                String jsonString = (String) originalValue;
                
                // Try to fix malformed JSON
                String fixedJson = fixMalformedJson(jsonString);
                if (fixedJson != null) {
                    // Retry processing with fixed JSON
                    return retryService.retryWithFixedData(
                        (String) deadLetterData.get("originalTopic"),
                        deadLetterData.get("originalKey"),
                        fixedJson
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Error handling JSON processing error", e);
        }
        return false;
    }

    private boolean handleValidationError(Map<String, Object> deadLetterData) {
        try {
            // For validation errors, try to create a valid default version
            Object originalValue = deadLetterData.get("originalValue");
            
            // Apply default values or corrections
            Map<String, Object> correctedData = applyValidationCorrections(originalValue);
            if (correctedData != null) {
                return retryService.retryWithFixedData(
                    (String) deadLetterData.get("originalTopic"),
                    deadLetterData.get("originalKey"),
                    correctedData
                );
            }
        } catch (Exception e) {
            logger.error("Error handling validation error", e);
        }
        return false;
    }

    private boolean handleDatabaseError(Map<String, Object> deadLetterData) {
        try {
            // For database errors, wait and retry
            Thread.sleep(5000); // Wait 5 seconds
            
            return retryService.retryOriginalData(
                (String) deadLetterData.get("originalTopic"),
                deadLetterData.get("originalKey"),
                deadLetterData.get("originalValue")
            );
        } catch (Exception e) {
            logger.error("Error handling database error", e);
        }
        return false;
    }

    private boolean handleGenericError(Map<String, Object> deadLetterData) {
        try {
            // For generic errors, log extensively and attempt one retry
            logger.info("Attempting generic error recovery for: {}", deadLetterData);
            
            return retryService.retryOriginalData(
                (String) deadLetterData.get("originalTopic"),
                deadLetterData.get("originalKey"),
                deadLetterData.get("originalValue")
            );
        } catch (Exception e) {
            logger.error("Error handling generic error", e);
        }
        return false;
    }

    private boolean attemptUserEventRecovery(Map<String, Object> deadLetterData) {
        // User events are less critical, apply basic recovery
        try {
            return retryService.retryOriginalData(
                (String) deadLetterData.get("originalTopic"),
                deadLetterData.get("originalKey"),
                deadLetterData.get("originalValue")
            );
        } catch (Exception e) {
            logger.error("Error in user event recovery", e);
            return false;
        }
    }

    private boolean attemptAlertRecovery(Map<String, Object> deadLetterData) {
        // Alerts are critical, try multiple strategies
        try {
            // First try original data
            if (retryService.retryOriginalData(
                (String) deadLetterData.get("originalTopic"),
                deadLetterData.get("originalKey"),
                deadLetterData.get("originalValue")
            )) {
                return true;
            }

            // If that fails, try to create a simplified alert
            Map<String, Object> simplifiedAlert = createSimplifiedAlert(deadLetterData);
            return retryService.retryWithFixedData(
                (String) deadLetterData.get("originalTopic"),
                deadLetterData.get("originalKey"),
                simplifiedAlert
            );

        } catch (Exception e) {
            logger.error("Error in alert recovery", e);
            return false;
        }
    }

    // Helper methods

    private String fixMalformedJson(String jsonString) {
        // Basic JSON fixing - add quotes to unquoted strings, fix common issues
        try {
            // This is a simplified example - in production, use a proper JSON repair library
            if (!jsonString.trim().startsWith("{") && !jsonString.trim().startsWith("[")) {
                return null;
            }
            // Add more sophisticated JSON repair logic here
            return jsonString;
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> applyValidationCorrections(Object originalValue) {
        // Apply default values for missing required fields
        // This is a simplified example
        return null;
    }

    private Map<String, Object> createSimplifiedAlert(Map<String, Object> deadLetterData) {
        Map<String, Object> simplifiedAlert = new java.util.HashMap<>();
        simplifiedAlert.put("alertId", java.util.UUID.randomUUID().toString());
        simplifiedAlert.put("type", "DLT_RECOVERY_ALERT");
        simplifiedAlert.put("severity", "HIGH");
        simplifiedAlert.put("timestamp", LocalDateTime.now());
        simplifiedAlert.put("description", "Alert recovered from dead letter queue");
        simplifiedAlert.put("originalError", deadLetterData.get("errorMessage"));
        return simplifiedAlert;
    }

    private void saveForManualIntervention(Map<String, Object> deadLetterData) {
        deadLetterService.saveFailedMessage(
            (String) deadLetterData.get("originalTopic"),
            deadLetterData.get("originalKey"),
            deadLetterData.get("originalValue"),
            "DLT processing failed: " + deadLetterData.get("errorMessage")
        );
    }

    private void logUserEventFailure(Map<String, Object> deadLetterData) {
        logger.warn("User event failed DLT processing: {}", deadLetterData);
        // Could send to analytics system for pattern analysis
    }

    private void sendOperationalAlert(Map<String, Object> deadLetterData) {
        logger.error("CRITICAL: Alert message could not be recovered from DLT: {}", deadLetterData);
        // In production, send to monitoring system or operations team
    }
}
