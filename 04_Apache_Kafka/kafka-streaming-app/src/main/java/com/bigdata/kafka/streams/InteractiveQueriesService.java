package com.bigdata.kafka.streams;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interactive Queries Service for accessing Kafka Streams state stores
 * Provides real-time access to aggregated data and analytics
 */
@Service
public class InteractiveQueriesService {

    private static final Logger logger = LoggerFactory.getLogger(InteractiveQueriesService.class);

    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    /**
     * Get customer spending for a specific time window
     */
    public Map<String, Object> getCustomerSpending(String customerId, Duration duration) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
            
            ReadOnlyWindowStore<String, Double> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(
                    "customer-spending-store", 
                    QueryableStoreTypes.windowStore()
                )
            );

            Instant now = Instant.now();
            Instant start = now.minus(duration);
            
            double totalSpending = 0.0;
            int windowCount = 0;
            
            try (WindowStoreIterator<Double> iterator = store.fetch(customerId, start, now)) {
                while (iterator.hasNext()) {
                    KeyValue<Long, Double> record = iterator.next();
                    totalSpending += record.value;
                    windowCount++;
                }
            }
            
            result.put("customerId", customerId);
            result.put("totalSpending", totalSpending);
            result.put("windowCount", windowCount);
            result.put("duration", duration.toString());
            result.put("queryTime", Instant.now());
            
        } catch (Exception e) {
            logger.error("Error querying customer spending for customer: {}", customerId, e);
            result.put("error", "Unable to retrieve customer spending data");
        }
        
        return result;
    }

    /**
     * Get transaction counts for a customer in recent time windows
     */
    public Map<String, Object> getCustomerTransactionCounts(String customerId, Duration duration) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
            
            ReadOnlyWindowStore<String, Long> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(
                    "transaction-counts-store", 
                    QueryableStoreTypes.windowStore()
                )
            );

            Instant now = Instant.now();
            Instant start = now.minus(duration);
            
            List<Map<String, Object>> windows = new ArrayList<>();
            long totalTransactions = 0;
            
            try (WindowStoreIterator<Long> iterator = store.fetch(customerId, start, now)) {
                while (iterator.hasNext()) {
                    KeyValue<Long, Long> record = iterator.next();
                    
                    Map<String, Object> window = new HashMap<>();
                    window.put("timestamp", record.key);
                    window.put("count", record.value);
                    windows.add(window);
                    
                    totalTransactions += record.value;
                }
            }
            
            result.put("customerId", customerId);
            result.put("totalTransactions", totalTransactions);
            result.put("windows", windows);
            result.put("duration", duration.toString());
            result.put("queryTime", Instant.now());
            
        } catch (Exception e) {
            logger.error("Error querying transaction counts for customer: {}", customerId, e);
            result.put("error", "Unable to retrieve transaction count data");
        }
        
        return result;
    }

    /**
     * Get all customer spending data (top spenders)
     */
    public List<Map<String, Object>> getTopSpenders(int limit) {
        List<Map<String, Object>> topSpenders = new ArrayList<>();
        
        try {
            KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
            
            ReadOnlyWindowStore<String, Double> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(
                    "customer-spending-store", 
                    QueryableStoreTypes.windowStore()
                )
            );

            Map<String, Double> customerTotals = new HashMap<>();
            
            // Get recent window data
            Instant now = Instant.now();
            Instant start = now.minus(Duration.ofHours(24));
            
            try (KeyValueIterator<Windowed<String>, Double> iterator = store.fetchAll(start, now)) {
                while (iterator.hasNext()) {
                    KeyValue<Windowed<String>, Double> record = iterator.next();
                    String customerId = record.key.key();
                    Double amount = record.value;
                    
                    customerTotals.merge(customerId, amount, Double::sum);
                }
            }
            
            // Sort and limit results
            customerTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .forEach(entry -> {
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("customerId", entry.getKey());
                    customer.put("totalSpending", entry.getValue());
                    topSpenders.add(customer);
                });
                
        } catch (Exception e) {
            logger.error("Error querying top spenders", e);
        }
        
        return topSpenders;
    }

    /**
     * Get real-time analytics dashboard data
     */
    public Map<String, Object> getAnalyticsDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            // Customer spending summary
            dashboard.put("topSpenders", getTopSpenders(10));
            
            // System health
            KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
            dashboard.put("streamsState", kafkaStreams.state().toString());
            
            // Current time
            dashboard.put("timestamp", Instant.now());
            
        } catch (Exception e) {
            logger.error("Error generating analytics dashboard", e);
            dashboard.put("error", "Unable to generate dashboard data");
        }
        
        return dashboard;
    }

    /**
     * Check if Kafka Streams is ready for queries
     */
    public boolean isReady() {
        try {
            KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
            return kafkaStreams != null && kafkaStreams.state() == KafkaStreams.State.RUNNING;
        } catch (Exception e) {
            logger.error("Error checking streams readiness", e);
            return false;
        }
    }
}
