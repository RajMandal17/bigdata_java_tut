package com.bigdata.crypto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Comprehensive Real-time Crypto Exchange Analytics Platform
 * 
 * This application provides:
 * - Real-time trading analytics and market insights
 * - Advanced fraud detection and compliance monitoring
 * - Algorithmic trading signal generation
 * - Risk management and portfolio analytics
 * - Integration with Gitbitex cryptocurrency exchange
 * - High-performance data processing with Apache Spark
 * - Real-time dashboard and WebSocket updates
 * 
 * Architecture:
 * Gitbitex Exchange -> Kafka Streams -> Spark Analytics -> Redis/InfluxDB -> Dashboard
 * 
 * @author BigData Learning Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableFeignClients
@EnableJpaRepositories
@EnableMongoRepositories
@EnableTransactionManagement
public class CryptoAnalyticsPlatformApplication {

    public static void main(String[] args) {
        // Set Spark configuration for embedded mode
        System.setProperty("spark.sql.warehouse.dir", "file:///tmp/spark-warehouse");
        System.setProperty("derby.system.home", "file:///tmp/derby");
        
        SpringApplication.run(CryptoAnalyticsPlatformApplication.class, args);
    }
}
