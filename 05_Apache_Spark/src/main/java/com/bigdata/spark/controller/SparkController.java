package com.bigdata.spark.controller;

import com.bigdata.spark.service.SparkRDDService;
import com.bigdata.spark.service.SparkDataFrameService;
import com.bigdata.spark.service.SparkSQLService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Apache Spark Core operations
 * Provides APIs for RDD, DataFrame, and SQL operations
 */
@RestController
@RequestMapping("/api/spark")
@CrossOrigin(origins = "*")
public class SparkController {

    private static final Logger logger = LoggerFactory.getLogger(SparkController.class);

    @Autowired
    private SparkRDDService rddService;

    @Autowired
    private SparkDataFrameService dataFrameService;

    @Autowired
    private SparkSQLService sqlService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Apache Spark Big Data Platform",
                "timestamp", System.currentTimeMillis()
        ));
    }

    // ==================== RDD Operations ====================

    /**
     * Perform basic RDD operations
     */
    @GetMapping("/rdd/basic")
    @Timed(value = "spark.rdd.basic.time", description = "Time taken for basic RDD operations")
    public ResponseEntity<Map<String, Object>> basicRDDOperations() {
        logger.info("Executing basic RDD operations");
        try {
            Map<String, Object> result = rddService.performBasicRDDOperations();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "basic_rdd_operations",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in basic RDD operations: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "basic_rdd_operations"
            ));
        }
    }

    /**
     * Perform advanced RDD operations
     */
    @GetMapping("/rdd/advanced")
    @Timed(value = "spark.rdd.advanced.time", description = "Time taken for advanced RDD operations")
    public ResponseEntity<Map<String, Object>> advancedRDDOperations() {
        logger.info("Executing advanced RDD operations");
        try {
            Map<String, Object> result = rddService.performAdvancedRDDOperations();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "advanced_rdd_operations",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in advanced RDD operations: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "advanced_rdd_operations"
            ));
        }
    }

    /**
     * Demonstrate RDD partitioning
     */
    @GetMapping("/rdd/partitioning")
    @Timed(value = "spark.rdd.partitioning.time", description = "Time taken for RDD partitioning demo")
    public ResponseEntity<Map<String, Object>> rddPartitioning() {
        logger.info("Demonstrating RDD partitioning");
        try {
            Map<String, Object> result = rddService.demonstrateRDDPartitioning();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "rdd_partitioning",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in RDD partitioning demonstration: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "rdd_partitioning"
            ));
        }
    }

    /**
     * Demonstrate RDD caching
     */
    @GetMapping("/rdd/caching")
    @Timed(value = "spark.rdd.caching.time", description = "Time taken for RDD caching demo")
    public ResponseEntity<Map<String, Object>> rddCaching() {
        logger.info("Demonstrating RDD caching");
        try {
            Map<String, Object> result = rddService.demonstrateRDDCaching();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "rdd_caching",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in RDD caching demonstration: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "rdd_caching"
            ));
        }
    }

    // ==================== DataFrame Operations ====================

    /**
     * Perform basic DataFrame operations
     */
    @GetMapping("/dataframe/basic")
    @Timed(value = "spark.dataframe.basic.time", description = "Time taken for basic DataFrame operations")
    public ResponseEntity<Map<String, Object>> basicDataFrameOperations() {
        logger.info("Executing basic DataFrame operations");
        try {
            Map<String, Object> result = dataFrameService.performBasicDataFrameOperations();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "basic_dataframe_operations",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in basic DataFrame operations: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "basic_dataframe_operations"
            ));
        }
    }

    /**
     * Perform advanced DataFrame operations
     */
    @GetMapping("/dataframe/advanced")
    @Timed(value = "spark.dataframe.advanced.time", description = "Time taken for advanced DataFrame operations")
    public ResponseEntity<Map<String, Object>> advancedDataFrameOperations() {
        logger.info("Executing advanced DataFrame operations");
        try {
            Map<String, Object> result = dataFrameService.performAdvancedDataFrameOperations();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "advanced_dataframe_operations",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in advanced DataFrame operations: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "advanced_dataframe_operations"
            ));
        }
    }

    /**
     * Perform Dataset operations
     */
    @GetMapping("/dataset/operations")
    @Timed(value = "spark.dataset.operations.time", description = "Time taken for Dataset operations")
    public ResponseEntity<Map<String, Object>> datasetOperations() {
        logger.info("Executing Dataset operations");
        try {
            Map<String, Object> result = dataFrameService.performDatasetOperations();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "dataset_operations",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in Dataset operations: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "dataset_operations"
            ));
        }
    }

    /**
     * Demonstrate DataFrame I/O operations
     */
    @GetMapping("/dataframe/io")
    @Timed(value = "spark.dataframe.io.time", description = "Time taken for DataFrame I/O operations")
    public ResponseEntity<Map<String, Object>> dataFrameIO() {
        logger.info("Demonstrating DataFrame I/O operations");
        try {
            Map<String, Object> result = dataFrameService.demonstrateDataFrameIO();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "dataframe_io",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in DataFrame I/O operations: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "dataframe_io"
            ));
        }
    }

    // ==================== SQL Operations ====================

    /**
     * Perform basic SQL operations
     */
    @GetMapping("/sql/basic")
    @Timed(value = "spark.sql.basic.time", description = "Time taken for basic SQL operations")
    public ResponseEntity<Map<String, Object>> basicSQLOperations() {
        logger.info("Executing basic SQL operations");
        try {
            Map<String, Object> result = sqlService.performBasicSQLOperations();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "basic_sql_operations",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in basic SQL operations: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "basic_sql_operations"
            ));
        }
    }

    /**
     * Perform advanced SQL operations
     */
    @GetMapping("/sql/advanced")
    @Timed(value = "spark.sql.advanced.time", description = "Time taken for advanced SQL operations")
    public ResponseEntity<Map<String, Object>> advancedSQLOperations() {
        logger.info("Executing advanced SQL operations");
        try {
            Map<String, Object> result = sqlService.performAdvancedSQLOperations();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "advanced_sql_operations",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in advanced SQL operations: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "advanced_sql_operations"
            ));
        }
    }

    /**
     * Demonstrate User Defined Functions
     */
    @GetMapping("/sql/udf")
    @Timed(value = "spark.sql.udf.time", description = "Time taken for UDF demonstration")
    public ResponseEntity<Map<String, Object>> udfDemonstration() {
        logger.info("Demonstrating User Defined Functions");
        try {
            Map<String, Object> result = sqlService.demonstrateUDFs();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "udf_demonstration",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in UDF demonstration: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "udf_demonstration"
            ));
        }
    }

    /**
     * Demonstrate SQL optimization techniques
     */
    @GetMapping("/sql/optimization")
    @Timed(value = "spark.sql.optimization.time", description = "Time taken for SQL optimization demo")
    public ResponseEntity<Map<String, Object>> sqlOptimization() {
        logger.info("Demonstrating SQL optimization techniques");
        try {
            Map<String, Object> result = sqlService.demonstrateSQLOptimization();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "sql_optimization",
                    "data", result
            ));
        } catch (Exception e) {
            logger.error("Error in SQL optimization demonstration: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "sql_optimization"
            ));
        }
    }

    // ==================== Custom SQL Query Execution ====================

    /**
     * Execute custom SQL query
     */
    @PostMapping("/sql/query")
    @Timed(value = "spark.sql.custom.time", description = "Time taken for custom SQL query")
    public ResponseEntity<Map<String, Object>> executeCustomQuery(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Query is required",
                    "operation", "custom_sql_query"
            ));
        }

        logger.info("Executing custom SQL query: {}", query);
        try {
            // For security, we should validate and sanitize the query in production
            // Here we'll execute it directly for demonstration purposes
            org.apache.spark.sql.Dataset<org.apache.spark.sql.Row> result = 
                    sqlService.sparkSession.sql(query);
            
            // Limit results to prevent memory issues
            java.util.List<java.util.Map<String, Object>> resultList = result.limit(100)
                    .toJavaRDD().map(row -> {
                        java.util.Map<String, Object> map = new java.util.HashMap<>();
                        for (int i = 0; i < row.length(); i++) {
                            map.put(result.columns()[i], row.get(i));
                        }
                        return map;
                    }).collect();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "operation", "custom_sql_query",
                    "query", query,
                    "data", resultList,
                    "count", resultList.size()
            ));
        } catch (Exception e) {
            logger.error("Error executing custom SQL query: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "operation", "custom_sql_query",
                    "query", query
            ));
        }
    }
}
