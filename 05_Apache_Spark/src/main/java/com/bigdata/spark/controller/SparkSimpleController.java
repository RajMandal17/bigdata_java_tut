package com.bigdata.spark.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Simplified REST Controller for demonstrating the Apache Spark platform
 */
@RestController
@RequestMapping("/api/spark")
@CrossOrigin(origins = "*")
public class SparkSimpleController {

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "Apache Spark Big Data Platform");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Platform is running successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Basic information about the platform
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Apache Spark Big Data Platform");
        response.put("version", "1.0.0");
        response.put("features", new String[]{
            "RDD Operations",
            "DataFrame Processing", 
            "Spark SQL",
            "Streaming Analytics",
            "Machine Learning",
            "Graph Processing",
            "Performance Monitoring"
        });
        response.put("status", "Operational");
        return ResponseEntity.ok(response);
    }

    /**
     * Sample data processing endpoint
     */
    @GetMapping("/sample")
    public ResponseEntity<Map<String, Object>> sampleProcessing() {
        Map<String, Object> response = new HashMap<>();
        
        // Simulate basic data processing
        response.put("operation", "Sample Data Processing");
        response.put("recordsProcessed", 10000);
        response.put("executionTime", "1.2 seconds");
        response.put("result", "Successfully processed sample sales data");
        
        // Sample analytics results
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalRevenue", 125000.50);
        analytics.put("avgTransactionValue", 12.5);
        analytics.put("topCategory", "Electronics");
        analytics.put("topRegion", "North");
        
        response.put("analytics", analytics);
        return ResponseEntity.ok(response);
    }
}
