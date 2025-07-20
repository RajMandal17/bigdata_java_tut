package com.bigdata.spark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application class for Apache Spark Big Data Platform
 * 
 * This application provides a comprehensive platform for:
 * - Apache Spark RDD, DataFrame, and Dataset operations
 * - Spark SQL query execution
 * - Real-time streaming with Kafka integration
 * - Machine Learning with MLlib
 * - Graph processing with GraphX
 * - Performance optimization and monitoring
 * 
 * @author BigData Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
public class SparkBigDataApplication {

    public static void main(String[] args) {
        // Set system properties for Spark
        System.setProperty("spark.sql.warehouse.dir", "/tmp/spark-warehouse");
        System.setProperty("derby.system.home", "/tmp/derby");
        
        // Disable Hadoop native libraries warning
        System.setProperty("java.library.path", "");
        
        SpringApplication app = new SpringApplication(SparkBigDataApplication.class);
        
        // Set additional properties
        app.setAdditionalProfiles("spark");
        
        app.run(args);
    }
}
