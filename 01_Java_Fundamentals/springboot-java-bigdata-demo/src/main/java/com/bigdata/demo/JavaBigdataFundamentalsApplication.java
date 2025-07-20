package com.bigdata.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class JavaBigdataFundamentalsApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaBigdataFundamentalsApplication.class, args);
        System.out.println("\n" +
                "================================================================================\n" +
                "           JAVA BIG DATA FUNDAMENTALS DEMO APPLICATION STARTED\n" +
                "================================================================================\n" +
                "Access the application at: http://localhost:8080\n" +
                "H2 Database Console: http://localhost:8080/h2-console\n" +
                "Actuator Health: http://localhost:8080/actuator/health\n" +
                "Metrics: http://localhost:8080/actuator/metrics\n" +
                "================================================================================\n"
        );
    }
}
