package com.bigdata.spark.streaming;

import com.bigdata.spark.model.StreamingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

/**
 * Service class for Apache Spark Structured Streaming operations
 * Handles real-time data processing from Kafka and other streaming sources
 */
@Service
public class SparkStreamingService {

    private static final Logger logger = LoggerFactory.getLogger(SparkStreamingService.class);

    @Autowired
    private SparkSession sparkSession;

    @Value("${kafka.bootstrap.servers:localhost:9092}")
    private String kafkaBootstrapServers;

    @Value("${kafka.topic.events:events-topic}")
    private String eventsTopic;

    @Value("${kafka.topic.transactions:transactions-topic}")
    private String transactionsTopic;

    @Value("${streaming.checkpoint.location:/tmp/spark-streaming-checkpoint}")
    private String checkpointLocation;

    private final List<StreamingQuery> activeQueries = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Starts all streaming queries
     */
    @PostConstruct
    public void initializeStreaming() {
        logger.info("Initializing Spark Streaming services");
        try {
            // Start event processing stream
            startEventProcessingStream();
            
            // Start fraud detection stream
            startFraudDetectionStream();
            
            // Start real-time analytics stream
            startRealTimeAnalyticsStream();
            
        } catch (Exception e) {
            logger.error("Error initializing streaming services: ", e);
        }
    }

    /**
     * Processes streaming events from Kafka
     */
    public StreamingQuery startEventProcessingStream() {
        logger.info("Starting event processing stream");

        try {
            // Read from Kafka
            Dataset<Row> kafkaStream = sparkSession
                    .readStream()
                    .format("kafka")
                    .option("kafka.bootstrap.servers", kafkaBootstrapServers)
                    .option("subscribe", eventsTopic)
                    .option("startingOffsets", "latest")
                    .option("failOnDataLoss", "false")
                    .load();

            // Parse JSON messages
            Dataset<Row> events = kafkaStream
                    .select(
                            col("key").cast("string").alias("message_key"),
                            from_json(col("value").cast("string"), getEventSchema())
                                    .alias("event"),
                            col("timestamp").alias("kafka_timestamp"),
                            col("partition"),
                            col("offset")
                    )
                    .select(
                            col("message_key"),
                            col("event.*"),
                            col("kafka_timestamp"),
                            col("partition"),
                            col("offset")
                    );

            // Add processing timestamp
            Dataset<Row> processedEvents = events
                    .withColumn("processing_time", current_timestamp())
                    .withColumn("hour_of_day", hour(col("event_timestamp")))
                    .withColumn("day_of_week", dayofweek(col("event_timestamp")));

            // Write to console for monitoring
            StreamingQuery consoleQuery = processedEvents
                    .writeStream()
                    .outputMode("append")
                    .format("console")
                    .option("truncate", false)
                    .option("numRows", 20)
                    .trigger(Trigger.ProcessingTime("30 seconds"))
                    .start();

            // Write to database (PostgreSQL)
            StreamingQuery dbQuery = processedEvents
                    .writeStream()
                    .outputMode("append")
                    .format("jdbc")
                    .option("url", "jdbc:postgresql://localhost:5432/spark_db")
                    .option("dbtable", "spark_analytics.streaming_events")
                    .option("user", "spark_user")
                    .option("password", "spark_password")
                    .option("driver", "org.postgresql.Driver")
                    .option("checkpointLocation", checkpointLocation + "/events_db")
                    .trigger(Trigger.ProcessingTime("1 minute"))
                    .start();

            activeQueries.add(consoleQuery);
            activeQueries.add(dbQuery);

            logger.info("Event processing stream started successfully");
            return consoleQuery;

        } catch (Exception e) {
            logger.error("Error starting event processing stream: ", e);
            throw new RuntimeException("Failed to start event processing stream", e);
        }
    }

    /**
     * Real-time fraud detection stream
     */
    public StreamingQuery startFraudDetectionStream() {
        logger.info("Starting fraud detection stream");

        try {
            // Read transaction events
            Dataset<Row> transactionStream = sparkSession
                    .readStream()
                    .format("kafka")
                    .option("kafka.bootstrap.servers", kafkaBootstrapServers)
                    .option("subscribe", transactionsTopic)
                    .option("startingOffsets", "latest")
                    .load();

            // Parse transaction data
            Dataset<Row> transactions = transactionStream
                    .select(
                            from_json(col("value").cast("string"), getTransactionSchema())
                                    .alias("transaction"),
                            col("timestamp").alias("kafka_timestamp")
                    )
                    .select("transaction.*", "kafka_timestamp");

            // Fraud detection logic
            Dataset<Row> suspiciousTransactions = transactions
                    .withWatermark("kafka_timestamp", "10 minutes")
                    .groupBy(
                            col("customer_id"),
                            window(col("kafka_timestamp"), "5 minutes")
                    )
                    .agg(
                            count("*").alias("transaction_count"),
                            sum("total_amount").alias("total_amount"),
                            countDistinct("payment_method").alias("payment_methods"),
                            countDistinct("customer_location").alias("locations"),
                            max("total_amount").alias("max_amount")
                    )
                    .filter(
                            col("transaction_count").gt(10)
                                    .or(col("total_amount").gt(10000))
                                    .or(col("payment_methods").gt(2))
                                    .or(col("locations").gt(1))
                                    .or(col("max_amount").gt(5000))
                    )
                    .withColumn("alert_type", 
                            when(col("transaction_count").gt(10), "HIGH_FREQUENCY")
                            .when(col("total_amount").gt(10000), "HIGH_VOLUME")
                            .when(col("payment_methods").gt(2), "MULTIPLE_PAYMENT_METHODS")
                            .when(col("locations").gt(1), "MULTIPLE_LOCATIONS")
                            .otherwise("HIGH_AMOUNT"))
                    .withColumn("risk_score", 
                            when(col("transaction_count").gt(20), 10)
                            .when(col("total_amount").gt(20000), 9)
                            .when(col("transaction_count").gt(15), 8)
                            .when(col("total_amount").gt(15000), 7)
                            .when(col("transaction_count").gt(10), 6)
                            .otherwise(5));

            // Write alerts to console
            StreamingQuery alertQuery = suspiciousTransactions
                    .writeStream()
                    .outputMode("update")
                    .format("console")
                    .option("truncate", false)
                    .trigger(Trigger.ProcessingTime("30 seconds"))
                    .start();

            activeQueries.add(alertQuery);

            logger.info("Fraud detection stream started successfully");
            return alertQuery;

        } catch (Exception e) {
            logger.error("Error starting fraud detection stream: ", e);
            throw new RuntimeException("Failed to start fraud detection stream", e);
        }
    }

    /**
     * Real-time analytics and dashboards
     */
    public StreamingQuery startRealTimeAnalyticsStream() {
        logger.info("Starting real-time analytics stream");

        try {
            // Read events stream
            Dataset<Row> eventStream = sparkSession
                    .readStream()
                    .format("kafka")
                    .option("kafka.bootstrap.servers", kafkaBootstrapServers)
                    .option("subscribe", eventsTopic)
                    .option("startingOffsets", "latest")
                    .load();

            Dataset<Row> events = eventStream
                    .select(
                            from_json(col("value").cast("string"), getEventSchema())
                                    .alias("event"),
                            col("timestamp").alias("kafka_timestamp")
                    )
                    .select("event.*", "kafka_timestamp");

            // Real-time metrics calculation
            Dataset<Row> realTimeMetrics = events
                    .withWatermark("kafka_timestamp", "2 minutes")
                    .groupBy(
                            window(col("kafka_timestamp"), "1 minute"),
                            col("event_type"),
                            col("source")
                    )
                    .agg(
                            count("*").alias("event_count"),
                            countDistinct("user_id").alias("unique_users"),
                            countDistinct("session_id").alias("unique_sessions")
                    )
                    .withColumn("events_per_second", col("event_count").divide(60))
                    .withColumn("avg_events_per_user", 
                            col("event_count").divide(col("unique_users")))
                    .withColumn("metric_timestamp", current_timestamp());

            // User session analytics
            Dataset<Row> sessionMetrics = events
                    .withWatermark("kafka_timestamp", "30 minutes")
                    .groupBy(
                            col("user_id"),
                            col("session_id"),
                            window(col("kafka_timestamp"), "10 minutes", "1 minute")
                    )
                    .agg(
                            count("*").alias("events_in_session"),
                            countDistinct("event_type").alias("event_types"),
                            min("kafka_timestamp").alias("session_start"),
                            max("kafka_timestamp").alias("session_end")
                    )
                    .withColumn("session_duration_minutes",
                            (col("session_end").cast("long") - col("session_start").cast("long")) / 60);

            // Write metrics to memory table for real-time queries
            StreamingQuery metricsQuery = realTimeMetrics
                    .writeStream()
                    .outputMode("complete")
                    .format("memory")
                    .queryName("real_time_metrics")
                    .trigger(Trigger.ProcessingTime("10 seconds"))
                    .start();

            StreamingQuery sessionQuery = sessionMetrics
                    .writeStream()
                    .outputMode("append")
                    .format("memory")
                    .queryName("session_metrics")
                    .trigger(Trigger.ProcessingTime("30 seconds"))
                    .start();

            activeQueries.add(metricsQuery);
            activeQueries.add(sessionQuery);

            logger.info("Real-time analytics stream started successfully");
            return metricsQuery;

        } catch (Exception e) {
            logger.error("Error starting real-time analytics stream: ", e);
            throw new RuntimeException("Failed to start real-time analytics stream", e);
        }
    }

    /**
     * Gets current streaming metrics
     */
    public Map<String, Object> getCurrentMetrics() {
        try {
            // Query the in-memory tables
            Dataset<Row> currentMetrics = sparkSession.sql(
                    "SELECT * FROM real_time_metrics ORDER BY window.start DESC LIMIT 10"
            );

            Dataset<Row> currentSessions = sparkSession.sql(
                    "SELECT * FROM session_metrics ORDER BY session_start DESC LIMIT 10"
            );

            return Map.of(
                    "current_metrics", convertToList(currentMetrics),
                    "current_sessions", convertToList(currentSessions),
                    "active_queries_count", activeQueries.size(),
                    "streaming_status", getStreamingStatus()
            );

        } catch (Exception e) {
            logger.error("Error getting current metrics: ", e);
            return Map.of("error", "Failed to get current metrics: " + e.getMessage());
        }
    }

    /**
     * Gets streaming query status
     */
    public Map<String, Object> getStreamingStatus() {
        Map<String, Object> status = new HashMap<>();
        
        for (int i = 0; i < activeQueries.size(); i++) {
            StreamingQuery query = activeQueries.get(i);
            Map<String, Object> queryStatus = new HashMap<>();
            
            queryStatus.put("id", query.id().toString());
            queryStatus.put("name", query.name());
            queryStatus.put("isActive", query.isActive());
            
            if (query.lastProgress() != null) {
                queryStatus.put("batchId", query.lastProgress().batchId());
                queryStatus.put("inputRowsPerSecond", query.lastProgress().inputRowsPerSecond());
                queryStatus.put("processedRowsPerSecond", query.lastProgress().processedRowsPerSecond());
                queryStatus.put("timestamp", query.lastProgress().timestamp());
            }
            
            status.put("query_" + i, queryStatus);
        }
        
        return status;
    }

    /**
     * Stops all streaming queries
     */
    @PreDestroy
    public void stopAllStreaming() {
        logger.info("Stopping all streaming queries");
        
        for (StreamingQuery query : activeQueries) {
            try {
                if (query.isActive()) {
                    query.stop();
                    logger.info("Stopped streaming query: {}", query.name());
                }
            } catch (Exception e) {
                logger.error("Error stopping query {}: ", query.name(), e);
            }
        }
        
        activeQueries.clear();
    }

    /**
     * Stops a specific streaming query
     */
    public boolean stopStreamingQuery(String queryName) {
        try {
            for (StreamingQuery query : activeQueries) {
                if (queryName.equals(query.name()) && query.isActive()) {
                    query.stop();
                    activeQueries.remove(query);
                    logger.info("Stopped streaming query: {}", queryName);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("Error stopping query {}: ", queryName, e);
            return false;
        }
    }

    /**
     * Schema for streaming events
     */
    private StructType getEventSchema() {
        return new StructType(new StructField[]{
                DataTypes.createStructField("event_id", DataTypes.StringType, false),
                DataTypes.createStructField("event_type", DataTypes.StringType, false),
                DataTypes.createStructField("user_id", DataTypes.StringType, true),
                DataTypes.createStructField("session_id", DataTypes.StringType, true),
                DataTypes.createStructField("event_timestamp", DataTypes.TimestampType, false),
                DataTypes.createStructField("source", DataTypes.StringType, true),
                DataTypes.createStructField("ip_address", DataTypes.StringType, true),
                DataTypes.createStructField("user_agent", DataTypes.StringType, true),
                DataTypes.createStructField("event_data", DataTypes.StringType, true)
        });
    }

    /**
     * Schema for transaction events
     */
    private StructType getTransactionSchema() {
        return new StructType(new StructField[]{
                DataTypes.createStructField("transaction_id", DataTypes.StringType, false),
                DataTypes.createStructField("customer_id", DataTypes.StringType, false),
                DataTypes.createStructField("total_amount", DataTypes.DoubleType, false),
                DataTypes.createStructField("payment_method", DataTypes.StringType, true),
                DataTypes.createStructField("customer_location", DataTypes.StringType, true),
                DataTypes.createStructField("timestamp", DataTypes.TimestampType, false)
        });
    }

    /**
     * Converts DataFrame to List for JSON serialization
     */
    private List<Map<String, Object>> convertToList(Dataset<Row> df) {
        return df.toJavaRDD().map(row -> {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < row.length(); i++) {
                map.put(df.columns()[i], row.get(i));
            }
            return map;
        }).collect();
    }
}
