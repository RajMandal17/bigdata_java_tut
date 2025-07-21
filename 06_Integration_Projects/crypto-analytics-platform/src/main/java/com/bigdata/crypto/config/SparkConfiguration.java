package com.bigdata.crypto.config;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Apache Spark Configuration for real-time crypto analytics
 * 
 * Configures Spark Session for:
 * - Real-time streaming analytics
 * - Machine learning model training
 * - Large-scale data processing
 * - Integration with Kafka streams
 */
@Configuration
public class SparkConfiguration {

    @Value("${spark.app-name:crypto-analytics-spark}")
    private String appName;

    @Value("${spark.master:local[*]}")
    private String master;

    @Value("${spark.sql.warehouse.dir:file:///tmp/spark-warehouse}")
    private String warehouseDir;

    @Bean
    public SparkConf sparkConf() {
        return new SparkConf()
                .setAppName(appName)
                .setMaster(master)
                .set("spark.sql.warehouse.dir", warehouseDir)
                .set("spark.sql.adaptive.enabled", "true")
                .set("spark.sql.adaptive.coalescePartitions.enabled", "true")
                .set("spark.sql.streaming.forceDeleteTempCheckpointLocation", "true")
                .set("spark.sql.streaming.checkpointLocation", "/tmp/spark-checkpoint")
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .set("spark.sql.execution.arrow.pyspark.enabled", "true")
                .set("spark.sql.adaptive.skewJoin.enabled", "true")
                .set("spark.dynamicAllocation.enabled", "false")
                .set("spark.ui.enabled", "true")
                .set("spark.ui.port", "4040");
    }

    @Bean
    public SparkSession sparkSession(SparkConf sparkConf) {
        return SparkSession.builder()
                .config(sparkConf)
                .getOrCreate();
    }
}
