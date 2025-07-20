package com.bigdata.spark.optimization;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.storage.StorageLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Spark performance optimization and tuning
 */
@Service
public class SparkOptimizationService {

    @Autowired
    private SparkSession sparkSession;

    /**
     * Optimize Spark configuration for the current workload
     */
    public Map<String, Object> optimizeSparkConfiguration() {
        Map<String, Object> recommendations = new HashMap<>();
        
        try {
            var conf = sparkSession.sparkContext().getConf();
            Map<String, String> currentConfig = new HashMap<>();
            Map<String, String> optimizedConfig = new HashMap<>();
            
            // Analyze current configuration
            currentConfig.put("spark.serializer", conf.get("spark.serializer", "org.apache.spark.serializer.JavaSerializer"));
            currentConfig.put("spark.sql.adaptive.enabled", conf.get("spark.sql.adaptive.enabled", "false"));
            currentConfig.put("spark.sql.adaptive.coalescePartitions.enabled", conf.get("spark.sql.adaptive.coalescePartitions.enabled", "false"));
            currentConfig.put("spark.dynamicAllocation.enabled", conf.get("spark.dynamicAllocation.enabled", "false"));
            currentConfig.put("spark.executor.memory", conf.get("spark.executor.memory", "1g"));
            currentConfig.put("spark.executor.cores", conf.get("spark.executor.cores", "1"));
            
            // Generate optimized recommendations
            optimizedConfig.put("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
            optimizedConfig.put("spark.sql.adaptive.enabled", "true");
            optimizedConfig.put("spark.sql.adaptive.coalescePartitions.enabled", "true");
            optimizedConfig.put("spark.sql.adaptive.skewJoin.enabled", "true");
            optimizedConfig.put("spark.dynamicAllocation.enabled", "true");
            optimizedConfig.put("spark.dynamicAllocation.minExecutors", "1");
            optimizedConfig.put("spark.dynamicAllocation.maxExecutors", "10");
            optimizedConfig.put("spark.sql.autoBroadcastJoinThreshold", "50MB");
            
            recommendations.put("currentConfig", currentConfig);
            recommendations.put("optimizedConfig", optimizedConfig);
            recommendations.put("optimizationSummary", generateOptimizationSummary(currentConfig, optimizedConfig));
            
        } catch (Exception e) {
            recommendations.put("error", "Failed to analyze configuration: " + e.getMessage());
        }
        
        return recommendations;
    }

    /**
     * Analyze DataFrame partitioning and suggest optimizations
     */
    public Map<String, Object> analyzePartitioning(String tableName) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            // Create a sample DataFrame for analysis
            Dataset<Row> df = sparkSession.sql("SELECT * FROM range(1000000) as t(id)");
            df.cache();
            
            // Analyze current partitioning
            int currentPartitions = df.rdd().getNumPartitions();
            long recordCount = df.count();
            long recordsPerPartition = recordCount / currentPartitions;
            
            analysis.put("tableName", tableName);
            analysis.put("totalRecords", recordCount);
            analysis.put("currentPartitions", currentPartitions);
            analysis.put("recordsPerPartition", recordsPerPartition);
            
            // Calculate optimal partitions
            int optimalPartitions = calculateOptimalPartitions(recordCount);
            analysis.put("recommendedPartitions", optimalPartitions);
            
            // Analyze partition skew
            analysis.put("partitionSkewAnalysis", analyzePartitionSkew(df));
            
            // Recommendations
            List<String> recommendations = new ArrayList<>();
            if (recordsPerPartition < 10000) {
                recommendations.add("Consider reducing number of partitions - too many small partitions");
            }
            if (recordsPerPartition > 1000000) {
                recommendations.add("Consider increasing number of partitions - partitions too large");
            }
            if (currentPartitions != optimalPartitions) {
                recommendations.add(String.format("Repartition to %d partitions for optimal performance", optimalPartitions));
            }
            
            analysis.put("recommendations", recommendations);
            
        } catch (Exception e) {
            analysis.put("error", "Failed to analyze partitioning: " + e.getMessage());
        }
        
        return analysis;
    }

    /**
     * Optimize caching strategy for DataFrames
     */
    public Map<String, Object> optimizeCaching() {
        Map<String, Object> cachingAnalysis = new HashMap<>();
        
        try {
            // Analyze current cache usage
            var cacheManager = sparkSession.sharedState().cacheManager();
            
            // Create sample DataFrames to demonstrate caching
            Dataset<Row> largeDF = sparkSession.sql("SELECT * FROM range(1000000) as t(id)");
            Dataset<Row> smallDF = sparkSession.sql("SELECT * FROM range(1000) as t(id)");
            
            // Analyze memory usage before caching
            Map<String, Object> beforeCaching = getMemoryUsage();
            
            // Cache with different storage levels
            largeDF.cache(); // Default MEMORY_AND_DISK
            smallDF.persist(StorageLevel.MEMORY_ONLY());
            
            // Force materialization
            largeDF.count();
            smallDF.count();
            
            // Analyze memory usage after caching
            Map<String, Object> afterCaching = getMemoryUsage();
            
            cachingAnalysis.put("beforeCaching", beforeCaching);
            cachingAnalysis.put("afterCaching", afterCaching);
            
            // Recommendations for different scenarios
            List<Map<String, String>> cachingRecommendations = Arrays.asList(
                Map.of("scenario", "Large DataFrames accessed multiple times", 
                       "recommendation", "Use MEMORY_AND_DISK storage level"),
                Map.of("scenario", "Small lookup tables", 
                       "recommendation", "Use MEMORY_ONLY storage level"),
                Map.of("scenario", "Infrequently accessed DataFrames", 
                       "recommendation", "Use DISK_ONLY storage level"),
                Map.of("scenario", "Network-intensive operations", 
                       "recommendation", "Use MEMORY_AND_DISK_SER for serialized storage")
            );
            
            cachingAnalysis.put("recommendations", cachingRecommendations);
            
            // Cleanup
            largeDF.unpersist();
            smallDF.unpersist();
            
        } catch (Exception e) {
            cachingAnalysis.put("error", "Failed to analyze caching: " + e.getMessage());
        }
        
        return cachingAnalysis;
    }

    /**
     * Analyze and optimize join operations
     */
    public Map<String, Object> optimizeJoins() {
        Map<String, Object> joinAnalysis = new HashMap<>();
        
        try {
            // Create sample DataFrames for join analysis
            Dataset<Row> largeTable = sparkSession.sql(
                "SELECT id, CAST(RAND() * 1000 AS INT) as category FROM range(1000000)"
            );
            Dataset<Row> smallTable = sparkSession.sql(
                "SELECT id as category, CONCAT('Category_', id) as name FROM range(100)"
            );
            
            // Analyze different join strategies
            Map<String, Object> joinStrategies = new HashMap<>();
            
            // Broadcast join (for small tables)
            Dataset<Row> broadcastJoin = largeTable.join(
                sparkSession.sql("SELECT /*+ BROADCAST(small) */ * FROM small"),
                "category"
            );
            
            // Sort-merge join (for large tables)
            Dataset<Row> sortMergeJoin = largeTable.join(smallTable, "category");
            
            joinStrategies.put("broadcastJoinPartitions", broadcastJoin.rdd().getNumPartitions());
            joinStrategies.put("sortMergeJoinPartitions", sortMergeJoin.rdd().getNumPartitions());
            
            // Join optimization recommendations
            List<Map<String, String>> joinRecommendations = Arrays.asList(
                Map.of("joinType", "Small table join", 
                       "strategy", "Use broadcast join with /*+ BROADCAST */ hint"),
                Map.of("joinType", "Large table join", 
                       "strategy", "Use sort-merge join with proper partitioning"),
                Map.of("joinType", "Skewed data join", 
                       "strategy", "Use salting technique or adaptive query execution"),
                Map.of("joinType", "Multiple joins", 
                       "strategy", "Cache intermediate results and optimize join order")
            );
            
            joinAnalysis.put("joinStrategies", joinStrategies);
            joinAnalysis.put("recommendations", joinRecommendations);
            
        } catch (Exception e) {
            joinAnalysis.put("error", "Failed to analyze joins: " + e.getMessage());
        }
        
        return joinAnalysis;
    }

    /**
     * Comprehensive performance tuning recommendations
     */
    public Map<String, Object> getPerformanceTuningGuide() {
        Map<String, Object> guide = new HashMap<>();
        
        // Memory optimization
        Map<String, String> memoryOptimization = new HashMap<>();
        memoryOptimization.put("heap_sizing", "Set spark.executor.memory to 70-80% of container memory");
        memoryOptimization.put("off_heap", "Enable off-heap storage with spark.sql.columnVector.offheap.enabled=true");
        memoryOptimization.put("gc_tuning", "Use G1GC for large heaps: spark.executor.extraJavaOptions=-XX:+UseG1GC");
        
        // CPU optimization
        Map<String, String> cpuOptimization = new HashMap<>();
        cpuOptimization.put("cores_per_executor", "Use 4-6 cores per executor for optimal performance");
        cpuOptimization.put("parallelism", "Set spark.default.parallelism to 2-3x number of cores");
        cpuOptimization.put("dynamic_allocation", "Enable dynamic allocation for variable workloads");
        
        // I/O optimization
        Map<String, String> ioOptimization = new HashMap<>();
        ioOptimization.put("file_format", "Use Parquet for columnar analytics workloads");
        ioOptimization.put("compression", "Use Snappy compression for good balance of speed and size");
        ioOptimization.put("partitioning", "Partition data by frequently filtered columns");
        
        // SQL optimization
        Map<String, String> sqlOptimization = new HashMap<>();
        sqlOptimization.put("adaptive_query", "Enable adaptive query execution (AQE)");
        sqlOptimization.put("cost_based_optimizer", "Enable cost-based optimizer (CBO)");
        sqlOptimization.put("predicate_pushdown", "Use filters early in query to reduce data scanned");
        
        guide.put("memoryOptimization", memoryOptimization);
        guide.put("cpuOptimization", cpuOptimization);
        guide.put("ioOptimization", ioOptimization);
        guide.put("sqlOptimization", sqlOptimization);
        
        return guide;
    }

    /**
     * Calculate optimal number of partitions based on data size
     */
    private int calculateOptimalPartitions(long recordCount) {
        // Rule of thumb: 100MB - 200MB per partition
        // Assuming average record size of 1KB
        long estimatedSizeBytes = recordCount * 1024;
        long targetPartitionSize = 128 * 1024 * 1024; // 128MB
        
        int optimalPartitions = (int) Math.ceil((double) estimatedSizeBytes / targetPartitionSize);
        
        // Ensure at least 1 partition and not more than available cores * 2
        int availableCores = sparkSession.sparkContext().defaultParallelism();
        return Math.max(1, Math.min(optimalPartitions, availableCores * 2));
    }

    /**
     * Analyze partition skew
     */
    private Map<String, Object> analyzePartitionSkew(Dataset<Row> df) {
        Map<String, Object> skewAnalysis = new HashMap<>();
        
        try {
            // Get partition sizes
            List<Integer> partitionSizes = df.rdd().mapPartitions(partition -> {
                int count = 0;
                while (partition.hasNext()) {
                    partition.next();
                    count++;
                }
                return Collections.singletonList(count).iterator();
            }, false).collect();
            
            // Calculate statistics
            int minSize = partitionSizes.stream().mapToInt(Integer::intValue).min().orElse(0);
            int maxSize = partitionSizes.stream().mapToInt(Integer::intValue).max().orElse(0);
            double avgSize = partitionSizes.stream().mapToInt(Integer::intValue).average().orElse(0);
            
            // Calculate skew ratio
            double skewRatio = maxSize > 0 ? (double) maxSize / avgSize : 0;
            
            skewAnalysis.put("minPartitionSize", minSize);
            skewAnalysis.put("maxPartitionSize", maxSize);
            skewAnalysis.put("avgPartitionSize", avgSize);
            skewAnalysis.put("skewRatio", skewRatio);
            skewAnalysis.put("isSkewed", skewRatio > 2.0);
            
        } catch (Exception e) {
            skewAnalysis.put("error", "Failed to analyze partition skew: " + e.getMessage());
        }
        
        return skewAnalysis;
    }

    /**
     * Get current memory usage
     */
    private Map<String, Object> getMemoryUsage() {
        Map<String, Object> memoryUsage = new HashMap<>();
        
        try {
            var statusTracker = sparkSession.sparkContext().statusTracker();
            var executorInfos = statusTracker.getExecutorInfos();
            
            long totalMaxMemory = 0;
            long totalMemoryUsed = 0;
            long totalStorageMemory = 0;
            
            for (var executorInfo : executorInfos) {
                totalMaxMemory += executorInfo.maxMemory();
                totalMemoryUsed += executorInfo.memoryUsed();
                totalStorageMemory += executorInfo.storageMemoryUsed();
            }
            
            memoryUsage.put("totalMaxMemoryMB", totalMaxMemory / (1024 * 1024));
            memoryUsage.put("totalMemoryUsedMB", totalMemoryUsed / (1024 * 1024));
            memoryUsage.put("totalStorageMemoryMB", totalStorageMemory / (1024 * 1024));
            memoryUsage.put("memoryUtilization", 
                totalMaxMemory > 0 ? (double) totalMemoryUsed / totalMaxMemory : 0);
            
        } catch (Exception e) {
            memoryUsage.put("error", "Failed to get memory usage: " + e.getMessage());
        }
        
        return memoryUsage;
    }

    /**
     * Generate optimization summary
     */
    private List<String> generateOptimizationSummary(Map<String, String> current, Map<String, String> optimized) {
        List<String> summary = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : optimized.entrySet()) {
            String key = entry.getKey();
            String optimizedValue = entry.getValue();
            String currentValue = current.get(key);
            
            if (!optimizedValue.equals(currentValue)) {
                summary.add(String.format("Change %s from '%s' to '%s'", key, currentValue, optimizedValue));
            }
        }
        
        return summary;
    }
}
