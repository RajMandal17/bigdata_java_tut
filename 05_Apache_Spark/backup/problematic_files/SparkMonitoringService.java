package com.bigdata.spark.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for monitoring Spark application performance and metrics
 */
@Service
public class SparkMonitoringService {

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private MeterRegistry meterRegistry;

    private final Counter jobSuccessCounter;
    private final Counter jobFailureCounter;
    private final Timer jobExecutionTimer;

    public SparkMonitoringService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.jobSuccessCounter = Counter.builder("spark.jobs.success")
            .description("Number of successful Spark jobs")
            .register(meterRegistry);
        this.jobFailureCounter = Counter.builder("spark.jobs.failure")
            .description("Number of failed Spark jobs")
            .register(meterRegistry);
        this.jobExecutionTimer = Timer.builder("spark.jobs.execution.time")
            .description("Spark job execution time")
            .register(meterRegistry);
    }

    /**
     * Get Spark application metrics
     */
    public Map<String, Object> getSparkMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Application info
            metrics.put("applicationId", sparkSession.sparkContext().applicationId());
            metrics.put("applicationName", sparkSession.sparkContext().appName());
            metrics.put("sparkVersion", sparkSession.version());
            metrics.put("masterUrl", sparkSession.sparkContext().master());
            
            // Resource info
            metrics.put("defaultParallelism", sparkSession.sparkContext().defaultParallelism());
            metrics.put("executorMemory", sparkSession.sparkContext().getConf().get("spark.executor.memory", "1g"));
            metrics.put("executorCores", sparkSession.sparkContext().getConf().get("spark.executor.cores", "1"));
            metrics.put("driverMemory", sparkSession.sparkContext().getConf().get("spark.driver.memory", "1g"));
            
            // Status info
            metrics.put("isActive", !sparkSession.sparkContext().isStopped());
            metrics.put("startTime", sparkSession.sparkContext().startTime());
            
            // Custom metrics
            metrics.put("successfulJobs", jobSuccessCounter.count());
            metrics.put("failedJobs", jobFailureCounter.count());
            
        } catch (Exception e) {
            metrics.put("error", "Failed to collect metrics: " + e.getMessage());
        }
        
        return metrics;
    }

    /**
     * Get detailed executor information
     */
    public Map<String, Object> getExecutorInfo() {
        Map<String, Object> executorInfo = new HashMap<>();
        
        try {
            // Get executor infos from status tracker
            var statusTracker = sparkSession.sparkContext().statusTracker();
            var executorInfos = statusTracker.getExecutorInfos();
            
            executorInfo.put("totalExecutors", executorInfos.length);
            executorInfo.put("activeExecutors", 
                java.util.Arrays.stream(executorInfos)
                    .mapToInt(info -> info.isActive() ? 1 : 0)
                    .sum());
            
            long totalCores = java.util.Arrays.stream(executorInfos)
                .mapToLong(info -> info.maxTasks())
                .sum();
            executorInfo.put("totalCores", totalCores);
            
            long totalMemory = java.util.Arrays.stream(executorInfos)
                .mapToLong(info -> info.maxMemory())
                .sum();
            executorInfo.put("totalMemoryBytes", totalMemory);
            executorInfo.put("totalMemoryMB", totalMemory / (1024 * 1024));
            
        } catch (Exception e) {
            executorInfo.put("error", "Failed to get executor info: " + e.getMessage());
        }
        
        return executorInfo;
    }

    /**
     * Get Spark job history and statistics
     */
    public Map<String, Object> getJobStatistics() {
        Map<String, Object> jobStats = new HashMap<>();
        
        try {
            var statusTracker = sparkSession.sparkContext().statusTracker();
            var jobIds = statusTracker.getJobIdsForGroup(null);
            
            jobStats.put("totalJobs", jobIds.length);
            
            int runningJobs = 0;
            int succeededJobs = 0;
            int failedJobs = 0;
            
            for (int jobId : jobIds) {
                var jobInfo = statusTracker.getJobInfo(jobId);
                if (jobInfo.isDefined()) {
                    var status = jobInfo.get().status();
                    switch (status.toString()) {
                        case "RUNNING":
                            runningJobs++;
                            break;
                        case "SUCCEEDED":
                            succeededJobs++;
                            break;
                        case "FAILED":
                            failedJobs++;
                            break;
                    }
                }
            }
            
            jobStats.put("runningJobs", runningJobs);
            jobStats.put("succeededJobs", succeededJobs);
            jobStats.put("failedJobs", failedJobs);
            
        } catch (Exception e) {
            jobStats.put("error", "Failed to get job statistics: " + e.getMessage());
        }
        
        return jobStats;
    }

    /**
     * Get stage information
     */
    public Map<String, Object> getStageInfo() {
        Map<String, Object> stageInfo = new HashMap<>();
        
        try {
            var statusTracker = sparkSession.sparkContext().statusTracker();
            var activeStages = statusTracker.getActiveStages();
            
            stageInfo.put("activeStages", activeStages.length);
            
            if (activeStages.length > 0) {
                var stage = activeStages[0];
                stageInfo.put("currentStageId", stage.stageId());
                stageInfo.put("currentStageName", stage.name());
                stageInfo.put("currentStageAttempt", stage.attemptNumber());
                stageInfo.put("numTasks", stage.numTasks());
                stageInfo.put("numActiveTasks", stage.numActiveTasks());
                stageInfo.put("numCompleteTasks", stage.numCompleteTasks());
                stageInfo.put("numFailedTasks", stage.numFailedTasks());
            }
            
        } catch (Exception e) {
            stageInfo.put("error", "Failed to get stage info: " + e.getMessage());
        }
        
        return stageInfo;
    }

    /**
     * Monitor and optimize DataFrame operations
     */
    public Map<String, Object> analyzeQueryPerformance(String queryName) {
        Map<String, Object> performance = new HashMap<>();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Get query execution plans and metrics
            performance.put("queryName", queryName);
            performance.put("timestamp", System.currentTimeMillis());
            
            // Get current cache info
            var cacheManager = sparkSession.sharedState().cacheManager();
            performance.put("cachedTables", cacheManager.lookupCachedData(sparkSession.sessionState().catalog().listTables()).size());
            
            // Add configuration recommendations
            performance.put("recommendations", getPerformanceRecommendations());
            
            jobSuccessCounter.increment();
            
        } catch (Exception e) {
            jobFailureCounter.increment();
            performance.put("error", "Failed to analyze query performance: " + e.getMessage());
        } finally {
            sample.stop(jobExecutionTimer);
        }
        
        return performance;
    }

    /**
     * Get performance recommendations
     */
    private Map<String, String> getPerformanceRecommendations() {
        Map<String, String> recommendations = new HashMap<>();
        
        try {
            var conf = sparkSession.sparkContext().getConf();
            
            // Check serializer
            String serializer = conf.get("spark.serializer", "");
            if (!serializer.contains("KryoSerializer")) {
                recommendations.put("serializer", "Consider using KryoSerializer for better performance");
            }
            
            // Check dynamic allocation
            boolean dynamicAllocation = conf.getBoolean("spark.dynamicAllocation.enabled", false);
            if (!dynamicAllocation) {
                recommendations.put("dynamicAllocation", "Enable dynamic allocation for better resource utilization");
            }
            
            // Check SQL adaptive query execution
            boolean adaptiveQueryExecution = conf.getBoolean("spark.sql.adaptive.enabled", false);
            if (!adaptiveQueryExecution) {
                recommendations.put("adaptiveQuery", "Enable adaptive query execution for better performance");
            }
            
            // Check broadcast threshold
            long broadcastThreshold = conf.getLong("spark.sql.autoBroadcastJoinThreshold", 10485760L);
            if (broadcastThreshold < 50 * 1024 * 1024) {
                recommendations.put("broadcastThreshold", "Consider increasing broadcast join threshold for better join performance");
            }
            
        } catch (Exception e) {
            recommendations.put("error", "Failed to generate recommendations: " + e.getMessage());
        }
        
        return recommendations;
    }

    /**
     * Get memory usage statistics
     */
    public Map<String, Object> getMemoryUsage() {
        Map<String, Object> memoryInfo = new HashMap<>();
        
        try {
            var statusTracker = sparkSession.sparkContext().statusTracker();
            var executorInfos = statusTracker.getExecutorInfos();
            
            long totalMaxMemory = 0;
            long totalMemoryUsed = 0;
            
            for (var executorInfo : executorInfos) {
                totalMaxMemory += executorInfo.maxMemory();
                totalMemoryUsed += executorInfo.memoryUsed();
            }
            
            memoryInfo.put("totalMaxMemoryMB", totalMaxMemory / (1024 * 1024));
            memoryInfo.put("totalMemoryUsedMB", totalMemoryUsed / (1024 * 1024));
            memoryInfo.put("memoryUtilizationPercent", 
                totalMaxMemory > 0 ? (double) totalMemoryUsed / totalMaxMemory * 100 : 0);
            
            // Storage memory info
            long storageMemoryUsed = java.util.Arrays.stream(executorInfos)
                .mapToLong(info -> info.storageMemoryUsed())
                .sum();
            memoryInfo.put("storageMemoryUsedMB", storageMemoryUsed / (1024 * 1024));
            
        } catch (Exception e) {
            memoryInfo.put("error", "Failed to get memory usage: " + e.getMessage());
        }
        
        return memoryInfo;
    }

    /**
     * Record job execution metrics
     */
    public void recordJobExecution(String jobName, boolean success, long executionTimeMs) {
        if (success) {
            jobSuccessCounter.increment();
        } else {
            jobFailureCounter.increment();
        }
        
        Timer.builder("spark.job.execution")
            .tag("job", jobName)
            .tag("status", success ? "success" : "failure")
            .register(meterRegistry)
            .record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
