package com.bigdata.spark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Simplified Apache Spark Big Data Application
 */
@SpringBootApplication
public class SparkBigDataApplicationSimple {

    public static void main(String[] args) {
        // Set system properties for Spark
        System.setProperty("spark.sql.warehouse.dir", "/tmp/spark-warehouse");
        System.setProperty("derby.system.home", "/tmp/derby");
        
        SpringApplication.run(SparkBigDataApplicationSimple.class, args);
    }
}
