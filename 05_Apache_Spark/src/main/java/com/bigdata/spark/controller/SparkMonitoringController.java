package com.bigdata.spark.controller;

import com.bigdata.spark.monitoring.SparkMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Spark monitoring and performance metrics
 */
@RestController
@RequestMapping("/api/spark/monitoring")
@CrossOrigin(origins = "*")
public class SparkMonitoringController {

    @Autowired
    private SparkMonitoringService sparkMonitoringService;

    /**
     * Get general Spark application metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getSparkMetrics() {
        try {
            Map<String, Object> metrics = sparkMonitoringService.getSparkMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get Spark metrics: " + e.getMessage()));
        }
    }

    /**
     * Get executor information
     */
    @GetMapping("/executors")
    public ResponseEntity<Map<String, Object>> getExecutorInfo() {
        try {
            Map<String, Object> executorInfo = sparkMonitoringService.getExecutorInfo();
            return ResponseEntity.ok(executorInfo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get executor info: " + e.getMessage()));
        }
    }

    /**
     * Get job statistics
     */
    @GetMapping("/jobs")
    public ResponseEntity<Map<String, Object>> getJobStatistics() {
        try {
            Map<String, Object> jobStats = sparkMonitoringService.getJobStatistics();
            return ResponseEntity.ok(jobStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get job statistics: " + e.getMessage()));
        }
    }

    /**
     * Get stage information
     */
    @GetMapping("/stages")
    public ResponseEntity<Map<String, Object>> getStageInfo() {
        try {
            Map<String, Object> stageInfo = sparkMonitoringService.getStageInfo();
            return ResponseEntity.ok(stageInfo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get stage info: " + e.getMessage()));
        }
    }

    /**
     * Analyze query performance
     */
    @GetMapping("/performance/{queryName}")
    public ResponseEntity<Map<String, Object>> analyzeQueryPerformance(@PathVariable String queryName) {
        try {
            Map<String, Object> performance = sparkMonitoringService.analyzeQueryPerformance(queryName);
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to analyze query performance: " + e.getMessage()));
        }
    }

    /**
     * Get memory usage statistics
     */
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemoryUsage() {
        try {
            Map<String, Object> memoryUsage = sparkMonitoringService.getMemoryUsage();
            return ResponseEntity.ok(memoryUsage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get memory usage: " + e.getMessage()));
        }
    }

    /**
     * Record job execution metrics
     */
    @PostMapping("/record")
    public ResponseEntity<Map<String, Object>> recordJobExecution(
            @RequestParam String jobName,
            @RequestParam boolean success,
            @RequestParam long executionTimeMs) {
        try {
            sparkMonitoringService.recordJobExecution(jobName, success, executionTimeMs);
            return ResponseEntity.ok(Map.of(
                "message", "Job execution recorded successfully",
                "jobName", jobName,
                "success", success,
                "executionTimeMs", executionTimeMs
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to record job execution: " + e.getMessage()));
        }
    }
}
