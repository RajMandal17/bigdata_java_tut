package com.bigdata.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableKafka
@EnableKafkaStreams
@EnableAsync
@EnableScheduling
@EnableRetry
public class KafkaStreamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamingApplication.class, args);
    }
}
