package com.bigdata.spark.controller;

import com.bigdata.spark.streaming.SparkStreamingService;
import com.bigdata.spark.ml.SparkMLService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Apache Spark Advanced features
 * Provides APIs for Streaming and Machine Learning operations
 */
@RestController
@RequestMapping("/api/spark")
@CrossOrigin(origins = "*")
public class SparkAdvancedController {

    private static final Logger logger = LoggerFactory.getLogger(SparkAdvancedController.class);

    @Autowired
    private SparkStreamingService streamingService;

    @Autowired
    private SparkMLService mlService;

    // ==================== Streaming Operations ====================

    /**
     * Get current streaming metrics
     */
    @GetMapping("/streaming/metrics")
    @Timed(value = "spark.streaming.metrics.time", description = "Time taken to get streaming metrics")
    public ResponseEntity<Map<String, Object>> getStreamingMetrics() {
        logger.info("Getting current streaming metrics");
        try {
            Map<String, Object> metrics = streamingService.getCurrentMetrics();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "streaming_metrics",
                    "data", metrics
            ));
        } catch (Exception e) {
            logger.error("Error getting streaming metrics: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "streaming_metrics"
            ));
        }
    }

    /**
     * Get streaming query status
     */
    @GetMapping("/streaming/status")
    @Timed(value = "spark.streaming.status.time", description = "Time taken to get streaming status")
    public ResponseEntity<Map<String, Object>> getStreamingStatus() {
        logger.info("Getting streaming query status");
        try {
            Map<String, Object> status = streamingService.getStreamingStatus();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "streaming_status",
                    "data", status
            ));
        } catch (Exception e) {
            logger.error("Error getting streaming status: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "streaming_status"
            ));
        }
    }

    /**
     * Start event processing stream
     */
    @PostMapping("/streaming/start/events")
    @Timed(value = "spark.streaming.start.events.time", description = "Time taken to start event processing stream")
    public ResponseEntity<Map<String, Object>> startEventProcessing() {
        logger.info("Starting event processing stream");
        try {
            streamingService.startEventProcessingStream();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "start_event_processing",
                    "message", "Event processing stream started successfully"
            ));
        } catch (Exception e) {
            logger.error("Error starting event processing stream: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "start_event_processing"
            ));
        }
    }

    /**
     * Start fraud detection stream
     */
    @PostMapping("/streaming/start/fraud")
    @Timed(value = "spark.streaming.start.fraud.time", description = "Time taken to start fraud detection stream")
    public ResponseEntity<Map<String, Object>> startFraudDetection() {
        logger.info("Starting fraud detection stream");
        try {
            streamingService.startFraudDetectionStream();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "start_fraud_detection",
                    "message", "Fraud detection stream started successfully"
            ));
        } catch (Exception e) {
            logger.error("Error starting fraud detection stream: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "start_fraud_detection"
            ));
        }
    }

    /**
     * Start real-time analytics stream
     */
    @PostMapping("/streaming/start/analytics")
    @Timed(value = "spark.streaming.start.analytics.time", description = "Time taken to start analytics stream")
    public ResponseEntity<Map<String, Object>> startRealTimeAnalytics() {
        logger.info("Starting real-time analytics stream");
        try {
            streamingService.startRealTimeAnalyticsStream();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "start_real_time_analytics",
                    "message", "Real-time analytics stream started successfully"
            ));
        } catch (Exception e) {
            logger.error("Error starting real-time analytics stream: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "start_real_time_analytics"
            ));
        }
    }

    /**
     * Stop streaming query
     */
    @PostMapping("/streaming/stop/{queryName}")
    @Timed(value = "spark.streaming.stop.time", description = "Time taken to stop streaming query")
    public ResponseEntity<Map<String, Object>> stopStreamingQuery(@PathVariable String queryName) {
        logger.info("Stopping streaming query: {}", queryName);
        try {
            boolean stopped = streamingService.stopStreamingQuery(queryName);
            return ResponseEntity.ok(Map.of(
                    "success", stopped,
                    "operation", "stop_streaming_query",
                    "query_name", queryName,
                    "message", stopped ? "Query stopped successfully" : "Query not found or already stopped"
            ));
        } catch (Exception e) {
            logger.error("Error stopping streaming query: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "stop_streaming_query",
                    "query_name", queryName
            ));
        }
    }

    /**
     * Stop all streaming queries
     */
    @PostMapping("/streaming/stop/all")
    @Timed(value = "spark.streaming.stop.all.time", description = "Time taken to stop all streaming queries")
    public ResponseEntity<Map<String, Object>> stopAllStreaming() {
        logger.info("Stopping all streaming queries");
        try {
            streamingService.stopAllStreaming();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "stop_all_streaming",
                    "message", "All streaming queries stopped successfully"
            ));
        } catch (Exception e) {
            logger.error("Error stopping all streaming queries: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "stop_all_streaming"
            ));
        }
    }

    // ==================== Machine Learning Operations ====================

    /**
     * Perform feature engineering
     */
    @GetMapping("/ml/feature-engineering")
    @Timed(value = "spark.ml.feature.time", description = "Time taken for feature engineering")
    public ResponseEntity<Map<String, Object>> featureEngineering() {
        logger.info("Performing feature engineering");
        try {
            Map<String, Object> result = mlService.performFeatureEngineering();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "feature_engineering",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in feature engineering: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "feature_engineering"
            ));
        }
    }

    /**
     * Perform fraud detection using ML
     */
    @GetMapping("/ml/fraud-detection")
    @Timed(value = "spark.ml.fraud.time", description = "Time taken for fraud detection ML")
    public ResponseEntity<Map<String, Object>> fraudDetection() {
        logger.info("Performing fraud detection with ML");
        try {
            Map<String, Object> result = mlService.performFraudDetection();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "fraud_detection_ml",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in fraud detection ML: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "fraud_detection_ml"
            ));
        }
    }

    /**
     * Perform customer segmentation
     */
    @GetMapping("/ml/customer-segmentation")
    @Timed(value = "spark.ml.segmentation.time", description = "Time taken for customer segmentation")
    public ResponseEntity<Map<String, Object>> customerSegmentation() {
        logger.info("Performing customer segmentation");
        try {
            Map<String, Object> result = mlService.performCustomerSegmentation();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "customer_segmentation",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in customer segmentation: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "customer_segmentation"
            ));
        }
    }

    /**
     * Build recommendation system
     */
    @GetMapping("/ml/recommendation-system")
    @Timed(value = "spark.ml.recommendation.time", description = "Time taken for recommendation system")
    public ResponseEntity<Map<String, Object>> recommendationSystem() {
        logger.info("Building recommendation system");
        try {
            Map<String, Object> result = mlService.buildRecommendationSystem();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "recommendation_system",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error building recommendation system: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "recommendation_system"
            ));
        }
    }

    /**
     * Perform sales prediction
     */
    @GetMapping("/ml/sales-prediction")
    @Timed(value = "spark.ml.prediction.time", description = "Time taken for sales prediction")
    public ResponseEntity<Map<String, Object>> salesPrediction() {
        logger.info("Performing sales prediction");
        try {
            Map<String, Object> result = mlService.performSalesPrediction();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "sales_prediction",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in sales prediction: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "sales_prediction"
            ));
        }
    }

    /**
     * Get all available ML operations
     */
    @GetMapping("/ml/operations")
    public ResponseEntity<Map<String, Object>> getMLOperations() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "operations", Map.of(
                        "feature_engineering", "Demonstrates feature engineering pipeline with various transformations",
                        "fraud_detection", "Binary classification for fraud detection using multiple algorithms",
                        "customer_segmentation", "K-means clustering for customer segmentation",
                        "recommendation_system", "Collaborative filtering using ALS algorithm",
                        "sales_prediction", "Linear regression for sales amount prediction"
                ),
                "endpoints", Map.of(
                        "feature_engineering", "/api/spark/ml/feature-engineering",
                        "fraud_detection", "/api/spark/ml/fraud-detection",
                        "customer_segmentation", "/api/spark/ml/customer-segmentation",
                        "recommendation_system", "/api/spark/ml/recommendation-system",
                        "sales_prediction", "/api/spark/ml/sales-prediction"
                )
        ));
    }

    /**
     * Get all available streaming operations
     */
    @GetMapping("/streaming/operations")
    public ResponseEntity<Map<String, Object>> getStreamingOperations() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "operations", Map.of(
                        "metrics", "Get current streaming metrics and statistics",
                        "status", "Get status of all active streaming queries",
                        "start_events", "Start event processing stream from Kafka",
                        "start_fraud", "Start real-time fraud detection stream",
                        "start_analytics", "Start real-time analytics and dashboard stream",
                        "stop_query", "Stop a specific streaming query by name",
                        "stop_all", "Stop all active streaming queries"
                ),
                "endpoints", Map.of(
                        "metrics", "/api/spark/streaming/metrics",
                        "status", "/api/spark/streaming/status",
                        "start_events", "/api/spark/streaming/start/events",
                        "start_fraud", "/api/spark/streaming/start/fraud",
                        "start_analytics", "/api/spark/streaming/start/analytics",
                        "stop_query", "/api/spark/streaming/stop/{queryName}",
                        "stop_all", "/api/spark/streaming/stop/all"
                )
        ));
    }
}
