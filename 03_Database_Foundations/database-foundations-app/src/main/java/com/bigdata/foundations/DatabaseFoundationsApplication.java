package com.bigdata.foundations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.bigdata.foundations.mysql.repository")
@EnableMongoRepositories(basePackages = "com.bigdata.foundations.mongodb.repository")
@EnableElasticsearchRepositories(basePackages = "com.bigdata.foundations.elasticsearch.repository")
@EnableKafka
public class DatabaseFoundationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseFoundationsApplication.class, args);
    }
}
