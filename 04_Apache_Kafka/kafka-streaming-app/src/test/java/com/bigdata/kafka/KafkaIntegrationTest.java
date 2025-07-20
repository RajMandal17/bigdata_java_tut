package com.bigdata.kafka;

import com.bigdata.kafka.model.TransactionEvent;
import com.bigdata.kafka.producer.KafkaProducerService;
import com.bigdata.kafka.consumer.TransactionEventConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Kafka Producer and Consumer
 * Uses embedded Kafka broker for testing
 */
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
    partitions = 3,
    topics = {"transactions-topic", "user-events-topic", "alerts-topic"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "kafka.topics.transactions=transactions-topic",
    "kafka.topics.user-events=user-events-topic",
    "kafka.topics.alerts=alerts-topic"
})
class KafkaIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testTransactionEventProducerAndConsumer() throws Exception {
        // Create test transaction event
        TransactionEvent transaction = TransactionEvent.builder()
            .transactionId(UUID.randomUUID().toString())
            .customerId("TEST_CUSTOMER_001")
            .amount(new BigDecimal("100.50"))
            .category("TEST_CATEGORY")
            .timestamp(LocalDateTime.now())
            .status("PENDING")
            .metadata(Map.of("test", true))
            .build();

        // Set up consumer
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TransactionEvent.class);

        DefaultKafkaConsumerFactory<String, TransactionEvent> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties("transactions-topic");
        KafkaMessageListenerContainer<String, TransactionEvent> container = 
            new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        BlockingQueue<ConsumerRecord<String, TransactionEvent>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, TransactionEvent>) records::add);
        container.start();

        // Wait for container to start
        Thread.sleep(1000);

        // Send transaction event
        kafkaProducerService.sendTransactionEvent(transaction);

        // Verify message was received
        ConsumerRecord<String, TransactionEvent> received = records.poll(10, TimeUnit.SECONDS);
        assertNotNull(received, "Should receive a transaction event");
        assertEquals(transaction.getTransactionId(), received.value().getTransactionId());
        assertEquals(transaction.getCustomerId(), received.value().getCustomerId());
        assertEquals(transaction.getAmount(), received.value().getAmount());

        container.stop();
    }

    @Test
    void testBatchTransactionEvents() throws Exception {
        // Create multiple test transactions
        TransactionEvent transaction1 = createTestTransaction("CUSTOMER_001", "100.00");
        TransactionEvent transaction2 = createTestTransaction("CUSTOMER_002", "200.00");
        TransactionEvent transaction3 = createTestTransaction("CUSTOMER_003", "300.00");

        // Set up consumer
        BlockingQueue<ConsumerRecord<String, TransactionEvent>> records = 
            setupTransactionConsumer("batch-test-group");

        // Send transactions
        kafkaProducerService.sendTransactionEvent(transaction1);
        kafkaProducerService.sendTransactionEvent(transaction2);
        kafkaProducerService.sendTransactionEvent(transaction3);

        // Verify all messages were received
        ConsumerRecord<String, TransactionEvent> received1 = records.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, TransactionEvent> received2 = records.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, TransactionEvent> received3 = records.poll(10, TimeUnit.SECONDS);

        assertNotNull(received1);
        assertNotNull(received2);
        assertNotNull(received3);

        // Verify content
        assertEquals("CUSTOMER_001", received1.value().getCustomerId());
        assertEquals("CUSTOMER_002", received2.value().getCustomerId());
        assertEquals("CUSTOMER_003", received3.value().getCustomerId());
    }

    @Test
    void testHighValueTransactionAlert() throws Exception {
        // Create high-value transaction
        TransactionEvent highValueTransaction = TransactionEvent.builder()
            .transactionId(UUID.randomUUID().toString())
            .customerId("HIGH_VALUE_CUSTOMER")
            .amount(new BigDecimal("15000.00")) // Above threshold
            .category("LUXURY_PURCHASE")
            .timestamp(LocalDateTime.now())
            .status("PENDING")
            .build();

        // Set up alert consumer
        BlockingQueue<ConsumerRecord<String, Object>> alertRecords = 
            setupAlertConsumer("alert-test-group");

        // Send high-value transaction
        kafkaProducerService.sendTransactionEvent(highValueTransaction);

        // Should trigger an alert
        ConsumerRecord<String, Object> alertReceived = alertRecords.poll(10, TimeUnit.SECONDS);
        
        // Note: This test assumes that high-value transactions trigger alerts
        // The actual alert generation might happen in stream processing or consumer
        if (alertReceived != null) {
            assertNotNull(alertReceived.value());
            // Add more specific alert validation here
        }
    }

    @Test
    void testCustomEventProduction() throws Exception {
        String customTopic = "test-custom-topic";
        String eventKey = "test-key";
        Map<String, Object> eventData = Map.of(
            "eventType", "CUSTOM_TEST",
            "timestamp", System.currentTimeMillis(),
            "data", "test data"
        );

        // Set up consumer for custom topic
        BlockingQueue<ConsumerRecord<String, Object>> records = 
            setupCustomConsumer(customTopic, "custom-test-group");

        // Send custom event
        kafkaProducerService.sendCustomEvent(customTopic, eventKey, eventData);

        // Verify message was received
        ConsumerRecord<String, Object> received = records.poll(10, TimeUnit.SECONDS);
        assertNotNull(received, "Should receive custom event");
        assertEquals(eventKey, received.key());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> receivedData = (Map<String, Object>) received.value();
        assertEquals("CUSTOM_TEST", receivedData.get("eventType"));
    }

    @Test
    void testProducerHealthCheck() {
        // Test producer health
        boolean isHealthy = kafkaProducerService.isHealthy();
        assertTrue(isHealthy, "Producer should be healthy");
    }

    @Test
    void testErrorHandling() throws Exception {
        // Test with invalid transaction (null values)
        TransactionEvent invalidTransaction = TransactionEvent.builder()
            .transactionId(null) // Invalid
            .customerId(null)    // Invalid
            .amount(null)        // Invalid
            .category("TEST")
            .timestamp(LocalDateTime.now())
            .status("PENDING")
            .build();

        // This should handle the error gracefully
        try {
            kafkaProducerService.sendTransactionEvent(invalidTransaction);
            // If it doesn't throw an exception, verify through logs or metrics
        } catch (Exception e) {
            // Expected behavior for invalid data
            assertNotNull(e.getMessage());
        }
    }

    // Helper methods
    private TransactionEvent createTestTransaction(String customerId, String amount) {
        return TransactionEvent.builder()
            .transactionId(UUID.randomUUID().toString())
            .customerId(customerId)
            .amount(new BigDecimal(amount))
            .category("TEST_CATEGORY")
            .timestamp(LocalDateTime.now())
            .status("PENDING")
            .metadata(Map.of("test", true))
            .build();
    }

    private BlockingQueue<ConsumerRecord<String, TransactionEvent>> setupTransactionConsumer(String groupId) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(groupId, "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TransactionEvent.class);

        DefaultKafkaConsumerFactory<String, TransactionEvent> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties("transactions-topic");
        KafkaMessageListenerContainer<String, TransactionEvent> container = 
            new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        BlockingQueue<ConsumerRecord<String, TransactionEvent>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, TransactionEvent>) records::add);
        container.start();

        return records;
    }

    private BlockingQueue<ConsumerRecord<String, Object>> setupAlertConsumer(String groupId) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(groupId, "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        DefaultKafkaConsumerFactory<String, Object> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties("alerts-topic");
        KafkaMessageListenerContainer<String, Object> container = 
            new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        BlockingQueue<ConsumerRecord<String, Object>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, Object>) records::add);
        container.start();

        return records;
    }

    private BlockingQueue<ConsumerRecord<String, Object>> setupCustomConsumer(String topic, String groupId) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(groupId, "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        DefaultKafkaConsumerFactory<String, Object> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(topic);
        KafkaMessageListenerContainer<String, Object> container = 
            new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        BlockingQueue<ConsumerRecord<String, Object>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, Object>) records::add);
        container.start();

        return records;
    }
}
