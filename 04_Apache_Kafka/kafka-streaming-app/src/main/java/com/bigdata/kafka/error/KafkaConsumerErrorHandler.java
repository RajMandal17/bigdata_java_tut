package com.bigdata.kafka.error;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Error Handler for processing message consumption errors
 * Implements retry logic and dead letter queue handling
 */
@Component
public class KafkaConsumerErrorHandler implements ConsumerAwareListenerErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerErrorHandler.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private DeadLetterService deadLetterService;

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception,
                            Consumer<?, ?> consumer) {

        ConsumerRecord<?, ?> record = (ConsumerRecord<?, ?>) message.getPayload();

        logger.error("Error processing message from topic: {}, partition: {}, offset: {}, key: {}",
                    record.topic(), record.partition(), record.offset(), record.key(), exception);

        try {
            // Determine if error is recoverable
            if (isRecoverableError(exception)) {
                handleRecoverableError(record, consumer, exception);
            } else {
                handleNonRecoverableError(record, exception);
            }
        } catch (Exception e) {
            logger.error("Error in error handler", e);
        }

        return null;
    }

    /**
     * Determines if an error is recoverable
     */
    private boolean isRecoverableError(Exception exception) {
        // Network errors, timeouts - recoverable
        if (exception.getCause() instanceof org.apache.kafka.common.errors.TimeoutException ||
            exception.getCause() instanceof java.net.SocketTimeoutException ||
            exception.getCause() instanceof java.net.ConnectException) {
            return true;
        }

        // Data format errors, validation errors - not recoverable
        if (exception.getCause() instanceof com.fasterxml.jackson.core.JsonProcessingException ||
            exception.getCause() instanceof IllegalArgumentException ||
            exception.getCause() instanceof javax.validation.ValidationException) {
            return false;
        }

        // Database connection errors - recoverable
        if (exception.getCause() instanceof java.sql.SQLException) {
            return true;
        }

        // Default to non-recoverable
        return false;
    }

    /**
     * Handle recoverable errors with retry logic
     */
    private void handleRecoverableError(ConsumerRecord<?, ?> record, Consumer<?, ?> consumer, 
                                      Exception exception) {
        logger.warn("Handling recoverable error for record from topic: {}, partition: {}, offset: {}",
                   record.topic(), record.partition(), record.offset());

        // For recoverable errors, we can implement custom retry logic
        // In this case, we'll let the default retry mechanism handle it
        // by not acknowledging the message

        // Optionally, we could seek to retry the message
        TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
        consumer.seek(topicPartition, record.offset());

        // Log retry attempt
        Map<String, Object> retryData = new HashMap<>();
        retryData.put("topic", record.topic());
        retryData.put("partition", record.partition());
        retryData.put("offset", record.offset());
        retryData.put("key", record.key());
        retryData.put("errorType", "RECOVERABLE");
        retryData.put("errorMessage", exception.getMessage());
        retryData.put("timestamp", LocalDateTime.now());

        deadLetterService.logRetryAttempt(retryData);
    }

    /**
     * Handle non-recoverable errors by sending to dead letter topic
     */
    private void handleNonRecoverableError(ConsumerRecord<?, ?> record, Exception exception) {
        logger.error("Handling non-recoverable error for record from topic: {}, partition: {}, offset: {}",
                    record.topic(), record.partition(), record.offset());

        try {
            // Send to dead letter topic
            String deadLetterTopic = record.topic() + "-dlt";
            
            // Create dead letter record with error information
            Map<String, Object> deadLetterData = new HashMap<>();
            deadLetterData.put("originalTopic", record.topic());
            deadLetterData.put("originalPartition", record.partition());
            deadLetterData.put("originalOffset", record.offset());
            deadLetterData.put("originalKey", record.key());
            deadLetterData.put("originalValue", record.value());
            deadLetterData.put("errorMessage", exception.getMessage());
            deadLetterData.put("errorType", exception.getClass().getSimpleName());
            deadLetterData.put("timestamp", LocalDateTime.now());
            deadLetterData.put("stackTrace", getStackTrace(exception));

            kafkaTemplate.send(deadLetterTopic, record.key(), deadLetterData);
            
            logger.info("Sent message to dead letter topic: {}", deadLetterTopic);

        } catch (Exception e) {
            logger.error("Failed to send message to dead letter topic", e);
            
            // As last resort, save to database
            deadLetterService.saveFailedMessage(record, exception);
        }
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception exception) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
}
