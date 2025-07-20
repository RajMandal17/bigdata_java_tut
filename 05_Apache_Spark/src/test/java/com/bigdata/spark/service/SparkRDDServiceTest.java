package com.bigdata.spark.service;

import com.bigdata.spark.config.SparkConfig;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SparkRDDService
 */
@ExtendWith(SpringJUnitExtension.class)
@SpringBootTest(classes = {SparkConfig.class, SparkRDDService.class})
public class SparkRDDServiceTest {

    @Autowired
    private SparkRDDService sparkRDDService;

    @Autowired
    private JavaSparkContext javaSparkContext;

    @Test
    public void testCreateRDD() {
        // Test RDD creation
        Map<String, Object> result = sparkRDDService.createSampleRDD();
        
        assertNotNull(result);
        assertTrue(result.containsKey("count"));
        assertTrue(result.containsKey("data"));
        
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(10, data.size());
        assertEquals(Integer.valueOf(10), result.get("count"));
    }

    @Test
    public void testRDDTransformations() {
        // Test map and filter transformations
        Map<String, Object> result = sparkRDDService.performTransformations();
        
        assertNotNull(result);
        assertTrue(result.containsKey("original"));
        assertTrue(result.containsKey("squared"));
        assertTrue(result.containsKey("filtered"));
        
        @SuppressWarnings("unchecked")
        List<Integer> filtered = (List<Integer>) result.get("filtered");
        // Should contain even numbers squared
        assertTrue(filtered.contains(4));  // 2^2
        assertTrue(filtered.contains(16)); // 4^2
        assertTrue(filtered.contains(36)); // 6^2
    }

    @Test
    public void testRDDActions() {
        // Test actions like reduce, collect, count
        Map<String, Object> result = sparkRDDService.performActions();
        
        assertNotNull(result);
        assertTrue(result.containsKey("count"));
        assertTrue(result.containsKey("sum"));
        assertTrue(result.containsKey("max"));
        assertTrue(result.containsKey("min"));
        
        assertEquals(Long.valueOf(10), result.get("count"));
        assertEquals(Integer.valueOf(10), result.get("max"));
        assertEquals(Integer.valueOf(1), result.get("min"));
    }

    @Test
    public void testWordCount() {
        // Test word count functionality
        Map<String, Object> result = sparkRDDService.performWordCount();
        
        assertNotNull(result);
        assertTrue(result.containsKey("wordCounts"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> wordCounts = (List<Map<String, Object>>) result.get("wordCounts");
        assertFalse(wordCounts.isEmpty());
        
        // Check that each word count entry has word and count
        for (Map<String, Object> wordCount : wordCounts) {
            assertTrue(wordCount.containsKey("word"));
            assertTrue(wordCount.containsKey("count"));
        }
    }

    @Test
    public void testJoinOperations() {
        // Test RDD join operations
        Map<String, Object> result = sparkRDDService.performJoinOperations();
        
        assertNotNull(result);
        assertTrue(result.containsKey("innerJoin"));
        assertTrue(result.containsKey("leftOuterJoin"));
        assertTrue(result.containsKey("rightOuterJoin"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> innerJoin = (List<Map<String, Object>>) result.get("innerJoin");
        assertFalse(innerJoin.isEmpty());
    }

    @Test
    public void testPartitionOperations() {
        // Test partition-related operations
        Map<String, Object> result = sparkRDDService.analyzePartitions();
        
        assertNotNull(result);
        assertTrue(result.containsKey("partitionCount"));
        assertTrue(result.containsKey("elementsPerPartition"));
        
        Integer partitionCount = (Integer) result.get("partitionCount");
        assertTrue(partitionCount > 0);
    }
}
