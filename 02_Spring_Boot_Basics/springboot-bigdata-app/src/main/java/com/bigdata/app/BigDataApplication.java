package com.bigdata.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot Big Data Application
 * 
 * This application demonstrates:
 * - REST API development
 * - Database integration (JPA + MongoDB)
 * - Kafka messaging
 * - Async processing
 * - Batch processing
 * - Monitoring and health checks
 * - Validation and error handling
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class BigDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(BigDataApplication.class, args);
        System.out.println("\n" +
            "================================================================================\n" +
            "           SPRING BOOT BIG DATA APPLICATION STARTED\n" +
            "================================================================================\n" +
            "Application: http://localhost:8082\n" +
            "H2 Console: http://localhost:8082/h2-console\n" +
            "Actuator Health: http://localhost:8082/actuator/health\n" +
            "Metrics: http://localhost:8082/actuator/metrics\n" +
            "Prometheus: http://localhost:8082/actuator/prometheus\n" +
            "================================================================================\n"
        );
    }
}
