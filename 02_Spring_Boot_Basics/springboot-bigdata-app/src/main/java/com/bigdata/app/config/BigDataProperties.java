package com.bigdata.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Custom configuration properties for Big Data application
 * Demonstrates externalized configuration management
 */
@ConfigurationProperties(prefix = "bigdata")
@Component
public class BigDataProperties {
    
    private int batchSize = 1000;
    private int maxConcurrentTasks = 10;
    private String dataDirectory = "/tmp/bigdata";
    private boolean kafkaEnabled = true;
    private int maxFileSize = 50; // MB
    private int processingTimeout = 300; // seconds
    
    // Getters and Setters
    public int getBatchSize() {
        return batchSize;
    }
    
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    public int getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }
    
    public void setMaxConcurrentTasks(int maxConcurrentTasks) {
        this.maxConcurrentTasks = maxConcurrentTasks;
    }
    
    public String getDataDirectory() {
        return dataDirectory;
    }
    
    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }
    
    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }
    
    public void setKafkaEnabled(boolean kafkaEnabled) {
        this.kafkaEnabled = kafkaEnabled;
    }
    
    public int getMaxFileSize() {
        return maxFileSize;
    }
    
    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    public int getProcessingTimeout() {
        return processingTimeout;
    }
    
    public void setProcessingTimeout(int processingTimeout) {
        this.processingTimeout = processingTimeout;
    }
}
