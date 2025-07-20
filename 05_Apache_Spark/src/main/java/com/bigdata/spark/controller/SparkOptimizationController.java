package com.bigdata.spark.controller;

import com.bigdata.spark.optimization.SparkOptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Spark performance optimization
 */
@RestController
@RequestMapping("/api/spark/optimization")
@CrossOrigin(origins = "*")
public class SparkOptimizationController {

    @Autowired
    private SparkOptimizationService sparkOptimizationService;

    /**
     * Get Spark configuration optimization recommendations
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> optimizeConfiguration() {
        try {
            Map<String, Object> optimization = sparkOptimizationService.optimizeSparkConfiguration();
            return ResponseEntity.ok(optimization);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to optimize configuration: " + e.getMessage()));
        }
    }

    /**
     * Analyze partitioning for a table
     */
    @GetMapping("/partitioning/{tableName}")
    public ResponseEntity<Map<String, Object>> analyzePartitioning(@PathVariable String tableName) {
        try {
            Map<String, Object> analysis = sparkOptimizationService.analyzePartitioning(tableName);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to analyze partitioning: " + e.getMessage()));
        }
    }

    /**
     * Get caching optimization recommendations
     */
    @GetMapping("/caching")
    public ResponseEntity<Map<String, Object>> optimizeCaching() {
        try {
            Map<String, Object> cachingOptimization = sparkOptimizationService.optimizeCaching();
            return ResponseEntity.ok(cachingOptimization);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to optimize caching: " + e.getMessage()));
        }
    }

    /**
     * Get join optimization recommendations
     */
    @GetMapping("/joins")
    public ResponseEntity<Map<String, Object>> optimizeJoins() {
        try {
            Map<String, Object> joinOptimization = sparkOptimizationService.optimizeJoins();
            return ResponseEntity.ok(joinOptimization);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to optimize joins: " + e.getMessage()));
        }
    }

    /**
     * Get comprehensive performance tuning guide
     */
    @GetMapping("/tuning-guide")
    public ResponseEntity<Map<String, Object>> getPerformanceTuningGuide() {
        try {
            Map<String, Object> guide = sparkOptimizationService.getPerformanceTuningGuide();
            return ResponseEntity.ok(guide);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get tuning guide: " + e.getMessage()));
        }
    }
}
