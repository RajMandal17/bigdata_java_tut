package com.bigdata.spark.config;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PreDestroy;

/**
 * Spark Configuration class for setting up Spark Session, Context, and Streaming Context
 * 
 * This configuration provides:
 * - SparkSession for SQL, DataFrame, and Dataset operations
 * - JavaSparkContext for RDD operations
 * - JavaStreamingContext for streaming applications
 * - Optimized configurations for production use
 */
@Configuration
public class SparkConfig {

    @Value("${spark.app.name:BigData-Spark-Platform}")
    private String appName;

    @Value("${spark.master:local[*]}")
    private String sparkMaster;

    @Value("${spark.streaming.batch.duration:10}")
    private int streamingBatchDuration;

    @Value("${spark.sql.warehouse.dir:/tmp/spark-warehouse}")
    private String warehouseDir;

    @Value("${spark.serializer:org.apache.spark.serializer.KryoSerializer}")
    private String serializer;

    @Value("${spark.sql.adaptive.enabled:true}")
    private boolean adaptiveQueryEnabled;

    @Value("${spark.sql.adaptive.coalescePartitions.enabled:true}")
    private boolean coalescePartitionsEnabled;

    private SparkSession sparkSession;
    private JavaStreamingContext streamingContext;

    /**
     * Creates and configures the main SparkSession
     */
    @Bean
    @Primary
    public SparkSession sparkSession() {
        if (sparkSession == null) {
            SparkConf conf = new SparkConf()
                    .setAppName(appName)
                    .setMaster(sparkMaster)
                    .set("spark.serializer", serializer)
                    .set("spark.sql.warehouse.dir", warehouseDir)
                    .set("spark.sql.adaptive.enabled", String.valueOf(adaptiveQueryEnabled))
                    .set("spark.sql.adaptive.coalescePartitions.enabled", String.valueOf(coalescePartitionsEnabled))
                    .set("spark.sql.adaptive.skewJoin.enabled", "true")
                    .set("spark.sql.adaptive.localShuffleReader.enabled", "true")
                    // Optimize for better performance
                    .set("spark.sql.execution.arrow.pyspark.enabled", "true")
                    .set("spark.sql.execution.arrow.maxRecordsPerBatch", "10000")
                    // Enable Delta Lake support
                    .set("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
                    .set("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
                    // Memory management
                    .set("spark.executor.memory", "2g")
                    .set("spark.driver.memory", "2g")
                    .set("spark.driver.maxResultSize", "2g")
                    // Network timeouts
                    .set("spark.network.timeout", "800s")
                    .set("spark.executor.heartbeatInterval", "60s")
                    // Enable event logging for monitoring
                    .set("spark.eventLog.enabled", "true")
                    .set("spark.eventLog.dir", "/tmp/spark-events")
                    // Kryo registration for better serialization performance
                    .set("spark.kryo.registrationRequired", "false")
                    .set("spark.kryo.unsafe", "true");

            sparkSession = SparkSession.builder()
                    .config(conf)
                    .enableHiveSupport()
                    .getOrCreate();

            // Set log level to reduce verbosity
            sparkSession.sparkContext().setLogLevel("WARN");
        }
        return sparkSession;
    }

    /**
     * Creates JavaSparkContext from SparkSession
     */
    @Bean
    public JavaSparkContext javaSparkContext(SparkSession sparkSession) {
        return new JavaSparkContext(sparkSession.sparkContext());
    }

    /**
     * Creates JavaStreamingContext for stream processing
     */
    @Bean
    public JavaStreamingContext javaStreamingContext(JavaSparkContext javaSparkContext) {
        if (streamingContext == null) {
            streamingContext = new JavaStreamingContext(
                    javaSparkContext, 
                    Duration.apply(streamingBatchDuration * 1000)
            );
            
            // Configure checkpointing for fault tolerance
            streamingContext.checkpoint("/tmp/spark-streaming-checkpoint");
        }
        return streamingContext;
    }

    /**
     * Cleanup method to properly close Spark resources
     */
    @PreDestroy
    public void cleanup() {
        try {
            if (streamingContext != null && !streamingContext.ssc().isActive()) {
                streamingContext.stop(true, true);
            }
            if (sparkSession != null) {
                sparkSession.stop();
            }
        } catch (Exception e) {
            // Log the exception but don't throw it during shutdown
            System.err.println("Error during Spark cleanup: " + e.getMessage());
        }
    }
}
