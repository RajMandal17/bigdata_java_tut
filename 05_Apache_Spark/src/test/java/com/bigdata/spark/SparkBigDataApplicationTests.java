package com.bigdata.spark;

import com.bigdata.spark.service.SparkRDDService;
import com.bigdata.spark.service.SparkDataFrameService;
import com.bigdata.spark.service.SparkSQLService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Apache Spark Big Data Platform
 */
@SpringBootTest
@ActiveProfiles("test")
public class SparkBigDataApplicationTests {

    @Autowired
    private SparkRDDService rddService;

    @Autowired
    private SparkDataFrameService dataFrameService;

    @Autowired
    private SparkSQLService sqlService;

    @Test
    public void contextLoads() {
        // Test that Spring context loads successfully
        assertNotNull(rddService);
        assertNotNull(dataFrameService);
        assertNotNull(sqlService);
    }

    @Test
    public void testBasicRDDOperations() {
        Map<String, Object> result = rddService.performBasicRDDOperations();
        
        assertNotNull(result);
        assertTrue(result.containsKey("original_count"));
        assertTrue(result.containsKey("even_numbers"));
        assertTrue(result.containsKey("sum_of_squares"));
        
        assertEquals(10L, result.get("original_count"));
        assertNotNull(result.get("even_numbers"));
    }

    @Test
    public void testBasicDataFrameOperations() {
        Map<String, Object> result = dataFrameService.performBasicDataFrameOperations();
        
        assertNotNull(result);
        assertTrue(result.containsKey("total_count"));
        assertTrue(result.containsKey("columns"));
        
        Long totalCount = (Long) result.get("total_count");
        assertTrue(totalCount > 0);
    }

    @Test
    public void testBasicSQLOperations() {
        Map<String, Object> result = sqlService.performBasicSQLOperations();
        
        assertNotNull(result);
        assertTrue(result.containsKey("total_sales_count"));
        assertTrue(result.containsKey("customer_sales"));
        
        Long salesCount = (Long) result.get("total_sales_count");
        assertTrue(salesCount > 0);
    }

    @Test
    public void testRDDPartitioning() {
        Map<String, Object> result = rddService.demonstrateRDDPartitioning();
        
        assertNotNull(result);
        assertTrue(result.containsKey("original_partitions"));
        assertTrue(result.containsKey("repartitioned_partitions"));
        assertTrue(result.containsKey("coalesced_partitions"));
        
        Integer originalPartitions = (Integer) result.get("original_partitions");
        Integer repartitionedPartitions = (Integer) result.get("repartitioned_partitions");
        Integer coalescedPartitions = (Integer) result.get("coalesced_partitions");
        
        assertEquals(4, originalPartitions.intValue());
        assertEquals(2, repartitionedPartitions.intValue());
        assertEquals(2, coalescedPartitions.intValue());
    }

    @Test
    public void testAdvancedDataFrameOperations() {
        Map<String, Object> result = dataFrameService.performAdvancedDataFrameOperations();
        
        assertNotNull(result);
        assertTrue(result.containsKey("windowed_data_sample"));
        assertTrue(result.containsKey("pivot_data"));
        assertTrue(result.containsKey("customer_summary"));
    }

    @Test
    public void testDatasetOperations() {
        Map<String, Object> result = dataFrameService.performDatasetOperations();
        
        assertNotNull(result);
        assertTrue(result.containsKey("unique_customers"));
        assertTrue(result.containsKey("total_amount_sum"));
        assertTrue(result.containsKey("high_value_sales_count"));
        
        Long uniqueCustomers = (Long) result.get("unique_customers");
        assertTrue(uniqueCustomers > 0);
    }

    @Test
    public void testAdvancedSQLOperations() {
        Map<String, Object> result = sqlService.performAdvancedSQLOperations();
        
        assertNotNull(result);
        assertTrue(result.containsKey("top_customers"));
        assertTrue(result.containsKey("ranked_sales"));
        assertTrue(result.containsKey("customer_segmentation"));
    }

    @Test
    public void testUDFDemonstration() {
        Map<String, Object> result = sqlService.demonstrateUDFs();
        
        assertNotNull(result);
        assertTrue(result.containsKey("sales_with_tax"));
        assertTrue(result.containsKey("sales_with_discount"));
        assertTrue(result.containsKey("customer_analytics"));
    }

    @Test
    public void testDataFrameIO() {
        Map<String, Object> result = dataFrameService.demonstrateDataFrameIO();
        
        assertNotNull(result);
        assertTrue(result.containsKey("original_count"));
        assertTrue(result.containsKey("parquet_count"));
        assertTrue(result.containsKey("json_count"));
        assertTrue(result.containsKey("csv_count"));
        
        Long originalCount = (Long) result.get("original_count");
        Long parquetCount = (Long) result.get("parquet_count");
        Long jsonCount = (Long) result.get("json_count");
        Long csvCount = (Long) result.get("csv_count");
        
        assertEquals(originalCount, parquetCount);
        assertEquals(originalCount, jsonCount);
        assertEquals(originalCount, csvCount);
    }
}
