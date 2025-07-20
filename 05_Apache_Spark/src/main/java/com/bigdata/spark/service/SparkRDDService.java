package com.bigdata.spark.service;

import com.bigdata.spark.model.SalesData;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service class for Apache Spark RDD operations
 * Demonstrates fundamental RDD transformations and actions
 */
@Service
public class SparkRDDService {

    private static final Logger logger = LoggerFactory.getLogger(SparkRDDService.class);

    @Autowired
    private JavaSparkContext sparkContext;

    /**
     * Demonstrates basic RDD transformations and actions
     */
    public Map<String, Object> performBasicRDDOperations() {
        logger.info("Performing basic RDD operations");

        try {
            // Create RDD from collection
            List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            JavaRDD<Integer> numbersRDD = sparkContext.parallelize(numbers);

            // Transformation: filter even numbers
            JavaRDD<Integer> evenNumbers = numbersRDD.filter(num -> num % 2 == 0);

            // Transformation: map to squares
            JavaRDD<Integer> squares = evenNumbers.map(num -> num * num);

            // Action: collect results
            List<Integer> result = squares.collect();

            // Action: reduce to sum
            Integer sum = squares.reduce((a, b) -> a + b);

            // Action: count
            long count = squares.count();

            logger.info("RDD operations completed successfully");
            return Map.of(
                    "original_count", numbersRDD.count(),
                    "even_numbers", evenNumbers.collect(),
                    "squares", result,
                    "sum_of_squares", sum,
                    "count", count
            );

        } catch (Exception e) {
            logger.error("Error in RDD operations: ", e);
            throw new RuntimeException("RDD operations failed", e);
        }
    }

    /**
     * Demonstrates advanced RDD operations with key-value pairs
     */
    public Map<String, Object> performAdvancedRDDOperations() {
        logger.info("Performing advanced RDD operations");

        try {
            // Create sample sales data
            List<SalesData> salesData = createSampleSalesData();
            JavaRDD<SalesData> salesRDD = sparkContext.parallelize(salesData);

            // PairRDD: Group by category
            JavaPairRDD<String, SalesData> categoryPairRDD = salesRDD.mapToPair(
                    (PairFunction<SalesData, String, SalesData>) sale -> 
                            new Tuple2<>(sale.getCategory(), sale)
            );

            // Reduce by key: Sum total amount by category
            JavaPairRDD<String, BigDecimal> categoryTotals = salesRDD.mapToPair(
                    (PairFunction<SalesData, String, BigDecimal>) sale -> 
                            new Tuple2<>(sale.getCategory(), sale.getTotalAmount())
            ).reduceByKey((Function2<BigDecimal, BigDecimal, BigDecimal>) BigDecimal::add);

            // Group by key: Group sales by customer
            JavaPairRDD<String, Iterable<SalesData>> customerGroups = salesRDD.mapToPair(
                    (PairFunction<SalesData, String, SalesData>) sale -> 
                            new Tuple2<>(sale.getCustomerId(), sale)
            ).groupByKey();

            // Count by key: Count sales by payment method
            Map<String, Long> paymentMethodCounts = salesRDD.mapToPair(
                    (PairFunction<SalesData, String, Integer>) sale -> 
                            new Tuple2<>(sale.getPaymentMethod(), 1)
            ).countByKey();

            // Join operations: Create customer and product RDDs for join example
            JavaPairRDD<String, String> customerLocationRDD = salesRDD.mapToPair(
                    (PairFunction<SalesData, String, String>) sale -> 
                            new Tuple2<>(sale.getCustomerId(), sale.getCustomerLocation())
            ).distinct();

            JavaPairRDD<String, BigDecimal> customerSpendingRDD = salesRDD.mapToPair(
                    (PairFunction<SalesData, String, BigDecimal>) sale -> 
                            new Tuple2<>(sale.getCustomerId(), sale.getTotalAmount())
            ).reduceByKey((Function2<BigDecimal, BigDecimal, BigDecimal>) BigDecimal::add);

            // Inner join
            JavaPairRDD<String, Tuple2<String, BigDecimal>> customerJoinRDD = 
                    customerLocationRDD.join(customerSpendingRDD);

            logger.info("Advanced RDD operations completed successfully");
            return Map.of(
                    "category_totals", categoryTotals.collectAsMap(),
                    "payment_method_counts", paymentMethodCounts,
                    "customer_groups_count", customerGroups.mapValues(group -> {
                        int count = 0;
                        for (SalesData sale : group) count++;
                        return count;
                    }).collectAsMap(),
                    "customer_location_spending", customerJoinRDD.collectAsMap(),
                    "total_sales_amount", salesRDD.map(SalesData::getTotalAmount)
                            .reduce((Function2<BigDecimal, BigDecimal, BigDecimal>) BigDecimal::add)
            );

        } catch (Exception e) {
            logger.error("Error in advanced RDD operations: ", e);
            throw new RuntimeException("Advanced RDD operations failed", e);
        }
    }

    /**
     * Demonstrates RDD partitioning and performance optimization
     */
    public Map<String, Object> demonstrateRDDPartitioning() {
        logger.info("Demonstrating RDD partitioning");

        try {
            List<String> data = Arrays.asList("apple", "banana", "cherry", "date", "elderberry", 
                                            "fig", "grape", "honeydew", "kiwi", "lemon");
            JavaRDD<String> dataRDD = sparkContext.parallelize(data, 4); // 4 partitions

            // Check partitions
            int numPartitions = dataRDD.getNumPartitions();
            List<List<String>> partitionData = dataRDD.glom().collect();

            // Repartition
            JavaRDD<String> repartitionedRDD = dataRDD.repartition(2);
            int newNumPartitions = repartitionedRDD.getNumPartitions();

            // Coalesce (reduce partitions)
            JavaRDD<String> coalescedRDD = dataRDD.coalesce(2);
            int coalescedPartitions = coalescedRDD.getNumPartitions();

            // Custom partitioner for pair RDD
            JavaPairRDD<String, Integer> pairRDD = dataRDD.mapToPair(
                    (PairFunction<String, String, Integer>) word -> 
                            new Tuple2<>(word.substring(0, 1), word.length())
            );

            // Partition by key (first letter)
            JavaPairRDD<String, Integer> partitionedByKey = pairRDD.partitionBy(
                    new org.apache.spark.HashPartitioner(3)
            );

            logger.info("RDD partitioning demonstration completed successfully");
            return Map.of(
                    "original_partitions", numPartitions,
                    "partition_data", partitionData,
                    "repartitioned_partitions", newNumPartitions,
                    "coalesced_partitions", coalescedPartitions,
                    "partitioned_by_key_data", partitionedByKey.collectAsMap()
            );

        } catch (Exception e) {
            logger.error("Error in RDD partitioning demonstration: ", e);
            throw new RuntimeException("RDD partitioning demonstration failed", e);
        }
    }

    /**
     * Demonstrates RDD caching and persistence
     */
    public Map<String, Object> demonstrateRDDCaching() {
        logger.info("Demonstrating RDD caching and persistence");

        try {
            // Create a computation-heavy RDD
            JavaRDD<Integer> heavyComputationRDD = sparkContext.parallelize(Arrays.asList(1, 2, 3, 4, 5))
                    .map((Function<Integer, Integer>) x -> {
                        // Simulate heavy computation
                        Thread.sleep(100);
                        return x * x * x;
                    });

            // Cache the RDD
            heavyComputationRDD.cache();

            // First action - will compute and cache
            long startTime1 = System.currentTimeMillis();
            List<Integer> result1 = heavyComputationRDD.collect();
            long time1 = System.currentTimeMillis() - startTime1;

            // Second action - should use cached data
            long startTime2 = System.currentTimeMillis();
            List<Integer> result2 = heavyComputationRDD.collect();
            long time2 = System.currentTimeMillis() - startTime2;

            // Different storage levels demonstration
            JavaRDD<Integer> memoryOnlyRDD = sparkContext.parallelize(Arrays.asList(1, 2, 3, 4, 5));
            memoryOnlyRDD.persist(org.apache.spark.storage.StorageLevel.MEMORY_ONLY());

            JavaRDD<Integer> memoryAndDiskRDD = sparkContext.parallelize(Arrays.asList(6, 7, 8, 9, 10));
            memoryAndDiskRDD.persist(org.apache.spark.storage.StorageLevel.MEMORY_AND_DISK());

            logger.info("RDD caching demonstration completed successfully");
            return Map.of(
                    "first_computation_time_ms", time1,
                    "second_computation_time_ms", time2,
                    "performance_improvement", time1 > time2 ? "Caching effective" : "No improvement",
                    "cached_result", result1,
                    "memory_only_count", memoryOnlyRDD.count(),
                    "memory_and_disk_count", memoryAndDiskRDD.count()
            );

        } catch (Exception e) {
            logger.error("Error in RDD caching demonstration: ", e);
            throw new RuntimeException("RDD caching demonstration failed", e);
        }
    }

    /**
     * Creates sample sales data for demonstrations
     */
    private List<SalesData> createSampleSalesData() {
        return Arrays.asList(
                new SalesData("TXN001", "CUST001", "PROD001", "Laptop", "Electronics", 
                             1, new BigDecimal("1200.00"), new BigDecimal("1200.00"), 
                             null, 35, "M", "New York", "Credit Card"),
                new SalesData("TXN002", "CUST002", "PROD002", "Coffee Maker", "Appliances", 
                             2, new BigDecimal("89.99"), new BigDecimal("179.98"), 
                             null, 28, "F", "California", "Debit Card"),
                new SalesData("TXN003", "CUST003", "PROD003", "Book", "Books", 
                             3, new BigDecimal("45.00"), new BigDecimal("135.00"), 
                             null, 42, "M", "Texas", "PayPal"),
                new SalesData("TXN004", "CUST001", "PROD004", "Mouse", "Electronics", 
                             1, new BigDecimal("25.99"), new BigDecimal("25.99"), 
                             null, 35, "M", "New York", "Credit Card"),
                new SalesData("TXN005", "CUST004", "PROD005", "Shoes", "Sports", 
                             1, new BigDecimal("120.00"), new BigDecimal("120.00"), 
                             null, 25, "F", "Florida", "Credit Card")
        );
    }
}
