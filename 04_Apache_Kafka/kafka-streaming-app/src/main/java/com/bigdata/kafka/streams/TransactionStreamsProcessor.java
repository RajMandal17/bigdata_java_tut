package com.bigdata.kafka.streams;

import com.bigdata.kafka.model.TransactionEvent;
import com.bigdata.kafka.model.UserEvent;
import com.bigdata.kafka.model.AlertEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Transaction Streams Processor for real-time transaction analysis
 * Implements fraud detection, spending analytics, and alerting
 */
@Component
public class TransactionStreamsProcessor {

    @Value("${kafka.topics.transactions:transactions-topic}")
    private String transactionsTopic;

    @Value("${kafka.topics.alerts:alerts-topic}")
    private String alertsTopic;

    @Value("${kafka.topics.enriched-transactions:enriched-transactions-topic}")
    private String enrichedTransactionsTopic;

    @Value("${kafka.topics.customer-analytics:customer-analytics-topic}")
    private String customerAnalyticsTopic;

    private final JsonSerde<TransactionEvent> transactionSerde = new JsonSerde<>(TransactionEvent.class);
    private final JsonSerde<AlertEvent> alertSerde = new JsonSerde<>(AlertEvent.class);

    @Autowired
    public void processTransactionStreams(StreamsBuilder builder) {
        
        // Configure serdes
        transactionSerde.configure(Map.of(), false);
        alertSerde.configure(Map.of(), false);

        // Input stream - all transactions
        KStream<String, TransactionEvent> transactions = builder
            .stream(transactionsTopic, Consumed.with(Serdes.String(), transactionSerde));

        // 1. High-value transaction detection
        processHighValueTransactions(transactions);

        // 2. Fraud detection based on transaction patterns
        processFraudDetection(transactions);

        // 3. Customer spending analytics with time windows
        processCustomerSpendingAnalytics(transactions);

        // 4. Transaction enrichment and validation
        processTransactionEnrichment(transactions);

        // 5. Real-time transaction categorization
        processTransactionCategorization(transactions);
    }

    /**
     * Detect and alert on high-value transactions
     */
    private void processHighValueTransactions(KStream<String, TransactionEvent> transactions) {
        KStream<String, AlertEvent> highValueAlerts = transactions
            .filter((key, transaction) -> 
                transaction.getAmount().compareTo(new BigDecimal("10000")) > 0)
            .mapValues(transaction -> AlertEvent.builder()
                .alertId(UUID.randomUUID().toString())
                .type("HIGH_VALUE_TRANSACTION")
                .severity("HIGH")
                .timestamp(LocalDateTime.now())
                .customerId(transaction.getCustomerId())
                .transactionId(transaction.getTransactionId())
                .amount(transaction.getAmount())
                .description("High-value transaction detected: $" + transaction.getAmount())
                .metadata(Map.of(
                    "category", transaction.getCategory(),
                    "originalTimestamp", transaction.getTimestamp().toString()
                ))
                .build());

        highValueAlerts.to(alertsTopic, Produced.with(Serdes.String(), alertSerde));
    }

    /**
     * Fraud detection based on rapid successive transactions
     */
    private void processFraudDetection(KStream<String, TransactionEvent> transactions) {
        
        // Group by customer and create time windows
        KTable<Windowed<String>, Long> transactionCounts = transactions
            .groupBy((key, transaction) -> transaction.getCustomerId())
            .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
            .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("transaction-counts-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.Long()));

        // Detect suspicious activity (more than 10 transactions in 5 minutes)
        KStream<String, AlertEvent> fraudAlerts = transactionCounts
            .toStream()
            .filter((windowedKey, count) -> count > 10)
            .map((windowedKey, count) -> KeyValue.pair(
                windowedKey.key(),
                AlertEvent.builder()
                    .alertId(UUID.randomUUID().toString())
                    .type("POTENTIAL_FRAUD")
                    .severity("CRITICAL")
                    .timestamp(LocalDateTime.now())
                    .customerId(windowedKey.key())
                    .description("Potential fraud detected: " + count + " transactions in 5 minutes")
                    .metadata(Map.of(
                        "transactionCount", count.toString(),
                        "windowStart", windowedKey.window().start(),
                        "windowEnd", windowedKey.window().end()
                    ))
                    .build()
            ));

        fraudAlerts.to(alertsTopic, Produced.with(Serdes.String(), alertSerde));
    }

    /**
     * Customer spending analytics with aggregations
     */
    private void processCustomerSpendingAnalytics(KStream<String, TransactionEvent> transactions) {
        
        // Aggregate spending by customer in hourly windows
        KTable<Windowed<String>, Double> customerSpending = transactions
            .groupBy((key, transaction) -> transaction.getCustomerId())
            .windowedBy(TimeWindows.of(Duration.ofHours(1)))
            .aggregate(
                () -> 0.0,
                (customerId, transaction, aggregate) -> 
                    aggregate + transaction.getAmount().doubleValue(),
                Materialized.<String, Double, WindowStore<Bytes, byte[]>>as("customer-spending-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(Serdes.Double())
            );

        // Create spending analytics events
        KStream<String, Map<String, Object>> spendingAnalytics = customerSpending
            .toStream()
            .map((windowedKey, amount) -> {
                Map<String, Object> analytics = new HashMap<>();
                analytics.put("customerId", windowedKey.key());
                analytics.put("totalSpending", amount);
                analytics.put("timeWindow", "1-hour");
                analytics.put("windowStart", windowedKey.window().start());
                analytics.put("windowEnd", windowedKey.window().end());
                analytics.put("timestamp", LocalDateTime.now());
                
                return KeyValue.pair(windowedKey.key(), analytics);
            });

        spendingAnalytics.to(customerAnalyticsTopic, 
            Produced.with(Serdes.String(), new JsonSerde<>(Map.class)));

        // Alert on excessive spending
        customerSpending
            .toStream()
            .filter((windowedKey, amount) -> amount > 50000.0)
            .mapValues((windowedKey, amount) -> AlertEvent.builder()
                .alertId(UUID.randomUUID().toString())
                .type("EXCESSIVE_SPENDING")
                .severity("HIGH")
                .timestamp(LocalDateTime.now())
                .customerId(windowedKey.key())
                .amount(BigDecimal.valueOf(amount))
                .description("Excessive spending detected: $" + amount + " in 1 hour")
                .build())
            .to(alertsTopic, Produced.with(Serdes.String(), alertSerde));
    }

    /**
     * Transaction enrichment with risk scoring
     */
    private void processTransactionEnrichment(KStream<String, TransactionEvent> transactions) {
        
        KStream<String, Map<String, Object>> enrichedTransactions = transactions
            .mapValues(transaction -> {
                Map<String, Object> enriched = new HashMap<>();
                enriched.put("originalTransaction", transaction);
                enriched.put("riskScore", calculateRiskScore(transaction));
                enriched.put("category", transaction.getCategory());
                enriched.put("timeOfDay", getTimeOfDay(transaction.getTimestamp()));
                enriched.put("isWeekend", isWeekend(transaction.getTimestamp()));
                enriched.put("enrichmentTimestamp", LocalDateTime.now());
                
                return enriched;
            });

        enrichedTransactions.to(enrichedTransactionsTopic, 
            Produced.with(Serdes.String(), new JsonSerde<>(Map.class)));
    }

    /**
     * Transaction categorization and analysis
     */
    private void processTransactionCategorization(KStream<String, TransactionEvent> transactions) {
        
        // Count transactions by category in 30-minute windows
        KTable<Windowed<String>, Long> categoryStats = transactions
            .groupBy((key, transaction) -> transaction.getCategory())
            .windowedBy(TimeWindows.of(Duration.ofMinutes(30)))
            .count();

        // Output category statistics
        categoryStats
            .toStream()
            .map((windowedKey, count) -> {
                Map<String, Object> stats = new HashMap<>();
                stats.put("category", windowedKey.key());
                stats.put("transactionCount", count);
                stats.put("timeWindow", "30-minutes");
                stats.put("timestamp", LocalDateTime.now());
                
                return KeyValue.pair(windowedKey.key(), stats);
            })
            .to("category-stats-topic", 
                Produced.with(Serdes.String(), new JsonSerde<>(Map.class)));
    }

    // Helper methods
    private double calculateRiskScore(TransactionEvent transaction) {
        double riskScore = 0.0;
        
        // Amount-based risk
        double amount = transaction.getAmount().doubleValue();
        if (amount > 10000) riskScore += 0.5;
        else if (amount > 5000) riskScore += 0.3;
        else if (amount > 1000) riskScore += 0.1;
        
        // Category-based risk
        String category = transaction.getCategory().toLowerCase();
        if (category.contains("gambling") || category.contains("cash")) {
            riskScore += 0.4;
        } else if (category.contains("online") || category.contains("crypto")) {
            riskScore += 0.2;
        }
        
        // Time-based risk
        int hour = transaction.getTimestamp().getHour();
        if (hour < 6 || hour > 23) {
            riskScore += 0.2;
        }
        
        return Math.min(riskScore, 1.0);
    }

    private String getTimeOfDay(LocalDateTime timestamp) {
        int hour = timestamp.getHour();
        if (hour >= 6 && hour < 12) return "MORNING";
        else if (hour >= 12 && hour < 18) return "AFTERNOON";
        else if (hour >= 18 && hour < 22) return "EVENING";
        else return "NIGHT";
    }

    private boolean isWeekend(LocalDateTime timestamp) {
        int dayOfWeek = timestamp.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7; // Saturday or Sunday
    }
}
