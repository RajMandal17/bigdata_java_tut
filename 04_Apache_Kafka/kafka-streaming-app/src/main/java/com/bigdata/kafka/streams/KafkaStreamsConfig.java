package com.bigdata.kafka.streams;

import com.bigdata.kafka.model.TransactionEvent;
import com.bigdata.kafka.model.UserEvent;
import com.bigdata.kafka.model.AlertEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Streams Configuration for real-time data processing
 * Configures stream processing topology and serialization
 */
@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.streams.application-id:bigdata-streams}")
    private String applicationId;

    @Value("${kafka.streams.num-stream-threads:3}")
    private int numStreamThreads;

    @Value("${kafka.streams.commit-interval-ms:1000}")
    private int commitIntervalMs;

    @Bean(name = "defaultKafkaStreamsConfig")
    public KafkaStreamsConfiguration kafkaStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        
        // Basic configuration
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Serialization
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);
        
        // Performance tuning
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, numStreamThreads);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, commitIntervalMs);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 16 * 1024 * 1024); // 16MB
        props.put(StreamsConfig.BUFFERED_RECORDS_PER_PARTITION_CONFIG, 1000);
        
        // State store configuration
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        
        // Error handling
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                org.apache.kafka.streams.errors.LogAndContinueExceptionHandler.class);
        props.put(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG,
                org.apache.kafka.streams.errors.DefaultProductionExceptionHandler.class);

        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public JsonSerde<TransactionEvent> transactionEventSerde() {
        JsonSerde<TransactionEvent> serde = new JsonSerde<>(TransactionEvent.class);
        serde.configure(Map.of(), false);
        return serde;
    }

    @Bean
    public JsonSerde<UserEvent> userEventSerde() {
        JsonSerde<UserEvent> serde = new JsonSerde<>(UserEvent.class);
        serde.configure(Map.of(), false);
        return serde;
    }

    @Bean
    public JsonSerde<AlertEvent> alertEventSerde() {
        JsonSerde<AlertEvent> serde = new JsonSerde<>(AlertEvent.class);
        serde.configure(Map.of(), false);
        return serde;
    }
}
