package com.bigdata.spark.service;

import com.bigdata.spark.config.SparkConfig;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SparkDataFrameService
 */
@ExtendWith(SpringJUnitExtension.class)
@SpringBootTest(classes = {SparkConfig.class, SparkDataFrameService.class})
public class SparkDataFrameServiceTest {

    @Autowired
    private SparkDataFrameService sparkDataFrameService;

    @Autowired
    private SparkSession sparkSession;

    @Test
    public void testCreateDataFrame() {
        Map<String, Object> result = sparkDataFrameService.createSampleDataFrame();
        
        assertNotNull(result);
        assertTrue(result.containsKey("count"));
        assertTrue(result.containsKey("schema"));
        assertTrue(result.containsKey("data"));
        
        Long count = (Long) result.get("count");
        assertTrue(count > 0);
    }

    @Test
    public void testDataFrameOperations() {
        Map<String, Object> result = sparkDataFrameService.performDataFrameOperations();
        
        assertNotNull(result);
        assertTrue(result.containsKey("totalRows"));
        assertTrue(result.containsKey("adultCount"));
        assertTrue(result.containsKey("averageAge"));
        assertTrue(result.containsKey("maxAge"));
        assertTrue(result.containsKey("minAge"));
    }

    @Test
    public void testGroupByOperations() {
        Map<String, Object> result = sparkDataFrameService.performGroupByOperations();
        
        assertNotNull(result);
        assertTrue(result.containsKey("departmentCounts"));
        assertTrue(result.containsKey("avgSalaryByDepartment"));
    }

    @Test
    public void testDataFrameJoins() {
        Map<String, Object> result = sparkDataFrameService.performJoinOperations();
        
        assertNotNull(result);
        assertTrue(result.containsKey("innerJoin"));
        assertTrue(result.containsKey("leftJoin"));
        assertTrue(result.containsKey("rightJoin"));
    }

    @Test
    public void testWindowFunctions() {
        Map<String, Object> result = sparkDataFrameService.performWindowOperations();
        
        assertNotNull(result);
        assertTrue(result.containsKey("rankedEmployees"));
        assertTrue(result.containsKey("runningTotal"));
    }
}
