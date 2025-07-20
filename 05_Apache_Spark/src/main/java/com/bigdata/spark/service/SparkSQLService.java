package com.bigdata.spark.service;

import org.apache.spark.sql.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.sql.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for Apache Spark SQL operations
 * Demonstrates SQL queries, temporary views, and advanced SQL features
 */
@Service
public class SparkSQLService {

    private static final Logger logger = LoggerFactory.getLogger(SparkSQLService.class);

    @Autowired
    public SparkSession sparkSession;

    /**
     * Demonstrates basic SQL operations with temporary views
     */
    public Map<String, Object> performBasicSQLOperations() {
        logger.info("Performing basic SQL operations");

        try {
            // Create sample data and register as temporary view
            setupSampleData();

            // Basic SELECT queries
            Dataset<Row> allSales = sparkSession.sql("SELECT * FROM sales");
            Dataset<Row> customerSales = sparkSession.sql(
                    "SELECT customer_id, customer_location, SUM(total_amount) as total_spent " +
                            "FROM sales GROUP BY customer_id, customer_location"
            );

            // WHERE clauses
            Dataset<Row> highValueSales = sparkSession.sql(
                    "SELECT * FROM sales WHERE total_amount > 100 ORDER BY total_amount DESC"
            );

            Dataset<Row> electronicsSales = sparkSession.sql(
                    "SELECT * FROM sales WHERE category = 'Electronics'"
            );

            // Aggregation queries
            Dataset<Row> categorySummary = sparkSession.sql(
                    "SELECT category, " +
                            "COUNT(*) as transaction_count, " +
                            "SUM(total_amount) as total_sales, " +
                            "AVG(total_amount) as avg_sales, " +
                            "MAX(total_amount) as max_sales, " +
                            "MIN(total_amount) as min_sales " +
                            "FROM sales " +
                            "GROUP BY category " +
                            "ORDER BY total_sales DESC"
            );

            // Date/Time operations
            Dataset<Row> salesByDate = sparkSession.sql(
                    "SELECT DATE(transaction_date) as sale_date, " +
                            "COUNT(*) as daily_transactions, " +
                            "SUM(total_amount) as daily_sales " +
                            "FROM sales " +
                            "GROUP BY DATE(transaction_date) " +
                            "ORDER BY sale_date"
            );

            logger.info("Basic SQL operations completed successfully");
            return Map.of(
                    "total_sales_count", allSales.count(),
                    "customer_sales", convertToList(customerSales),
                    "high_value_sales", convertToList(highValueSales),
                    "electronics_sales_count", electronicsSales.count(),
                    "category_summary", convertToList(categorySummary),
                    "sales_by_date", convertToList(salesByDate)
            );

        } catch (Exception e) {
            logger.error("Error in basic SQL operations: ", e);
            throw new RuntimeException("Basic SQL operations failed", e);
        }
    }

    /**
     * Demonstrates advanced SQL operations
     */
    public Map<String, Object> performAdvancedSQLOperations() {
        logger.info("Performing advanced SQL operations");

        try {
            setupSampleData();

            // Subqueries
            Dataset<Row> topCustomers = sparkSession.sql(
                    "SELECT * FROM sales " +
                            "WHERE customer_id IN (" +
                            "  SELECT customer_id FROM sales " +
                            "  GROUP BY customer_id " +
                            "  HAVING SUM(total_amount) > 500" +
                            ")"
            );

            // Window functions
            Dataset<Row> rankedSales = sparkSession.sql(
                    "SELECT *, " +
                            "ROW_NUMBER() OVER (PARTITION BY customer_id ORDER BY total_amount DESC) as purchase_rank, " +
                            "SUM(total_amount) OVER (PARTITION BY customer_id) as customer_total, " +
                            "LAG(total_amount) OVER (PARTITION BY customer_id ORDER BY transaction_date) as prev_purchase " +
                            "FROM sales"
            );

            // CTE (Common Table Expressions)
            Dataset<Row> cteExample = sparkSession.sql(
                    "WITH customer_stats AS (" +
                            "  SELECT customer_id, " +
                            "         COUNT(*) as purchase_count, " +
                            "         SUM(total_amount) as total_spent, " +
                            "         AVG(total_amount) as avg_spent " +
                            "  FROM sales GROUP BY customer_id" +
                            "), " +
                            "category_stats AS (" +
                            "  SELECT category, " +
                            "         COUNT(*) as product_count, " +
                            "         SUM(total_amount) as category_revenue " +
                            "  FROM sales GROUP BY category" +
                            ") " +
                            "SELECT cs.*, cat.category, cat.category_revenue " +
                            "FROM customer_stats cs " +
                            "CROSS JOIN category_stats cat " +
                            "WHERE cs.total_spent > 200"
            );

            // JOIN operations
            sparkSession.sql(
                    "CREATE OR REPLACE TEMPORARY VIEW customer_summary AS " +
                            "SELECT customer_id, customer_location, customer_age, " +
                            "       COUNT(*) as transaction_count, " +
                            "       SUM(total_amount) as total_spent " +
                            "FROM sales " +
                            "GROUP BY customer_id, customer_location, customer_age"
            );

            sparkSession.sql(
                    "CREATE OR REPLACE TEMPORARY VIEW product_summary AS " +
                            "SELECT product_id, product_name, category, " +
                            "       SUM(quantity) as total_quantity, " +
                            "       SUM(total_amount) as total_revenue " +
                            "FROM sales " +
                            "GROUP BY product_id, product_name, category"
            );

            Dataset<Row> joinedData = sparkSession.sql(
                    "SELECT cs.customer_location, ps.category, " +
                            "       SUM(cs.total_spent) as location_spending, " +
                            "       SUM(ps.total_revenue) as category_revenue " +
                            "FROM customer_summary cs " +
                            "CROSS JOIN product_summary ps " +
                            "GROUP BY cs.customer_location, ps.category " +
                            "ORDER BY location_spending DESC"
            );

            // CASE WHEN statements
            Dataset<Row> customerSegmentation = sparkSession.sql(
                    "SELECT customer_id, customer_age, total_spent, " +
                            "CASE " +
                            "  WHEN total_spent > 1000 THEN 'Premium' " +
                            "  WHEN total_spent > 500 THEN 'Gold' " +
                            "  WHEN total_spent > 200 THEN 'Silver' " +
                            "  ELSE 'Bronze' " +
                            "END as customer_tier " +
                            "FROM (" +
                            "  SELECT customer_id, customer_age, SUM(total_amount) as total_spent " +
                            "  FROM sales GROUP BY customer_id, customer_age" +
                            ") customer_totals"
            );

            logger.info("Advanced SQL operations completed successfully");
            return Map.of(
                    "top_customers", convertToList(topCustomers),
                    "ranked_sales", convertToList(rankedSales.limit(10)),
                    "cte_example", convertToList(cteExample),
                    "joined_data", convertToList(joinedData),
                    "customer_segmentation", convertToList(customerSegmentation)
            );

        } catch (Exception e) {
            logger.error("Error in advanced SQL operations: ", e);
            throw new RuntimeException("Advanced SQL operations failed", e);
        }
    }

    /**
     * Demonstrates User Defined Functions (UDFs)
     */
    public Map<String, Object> demonstrateUDFs() {
        logger.info("Demonstrating User Defined Functions");

        try {
            setupSampleData();

            // Register scalar UDFs
            sparkSession.udf().register("calculate_tax", 
                    (UDF2<Double, String, Double>) (amount, location) -> {
                        switch (location) {
                            case "New York": return amount * 0.08;
                            case "California": return amount * 0.0725;
                            case "Texas": return amount * 0.0625;
                            case "Florida": return amount * 0.06;
                            default: return amount * 0.05;
                        }
                    }, DataTypes.DoubleType);

            sparkSession.udf().register("categorize_age", 
                    (UDF1<Integer, String>) age -> {
                        if (age < 25) return "Young";
                        else if (age < 40) return "Adult";
                        else if (age < 60) return "Middle-aged";
                        else return "Senior";
                    }, DataTypes.StringType);

            sparkSession.udf().register("format_currency", 
                    (UDF1<Double, String>) amount -> 
                            String.format("$%.2f", amount), DataTypes.StringType);

            // Use UDFs in SQL queries
            Dataset<Row> salesWithTax = sparkSession.sql(
                    "SELECT *, " +
                            "calculate_tax(total_amount, customer_location) as tax_amount, " +
                            "total_amount + calculate_tax(total_amount, customer_location) as total_with_tax, " +
                            "categorize_age(customer_age) as age_group, " +
                            "format_currency(total_amount) as formatted_amount " +
                            "FROM sales"
            );

            // Aggregate UDF example
            sparkSession.udf().register("discount_rate", 
                    (UDF2<String, Integer, Double>) (paymentMethod, quantity) -> {
                        double baseDiscount = 0.0;
                        if ("Credit Card".equals(paymentMethod)) baseDiscount = 0.02;
                        else if ("PayPal".equals(paymentMethod)) baseDiscount = 0.015;
                        
                        if (quantity > 2) baseDiscount += 0.01;
                        return Math.min(baseDiscount, 0.05); // Max 5% discount
                    }, DataTypes.DoubleType);

            Dataset<Row> salesWithDiscount = sparkSession.sql(
                    "SELECT *, " +
                            "discount_rate(payment_method, quantity) as discount_rate, " +
                            "total_amount * discount_rate(payment_method, quantity) as discount_amount " +
                            "FROM sales"
            );

            // Aggregate functions with UDFs
            Dataset<Row> customerAnalytics = sparkSession.sql(
                    "SELECT customer_location, " +
                            "categorize_age(CAST(AVG(customer_age) AS INT)) as avg_age_group, " +
                            "COUNT(*) as transaction_count, " +
                            "format_currency(SUM(total_amount)) as total_sales, " +
                            "format_currency(AVG(total_amount)) as avg_transaction " +
                            "FROM sales " +
                            "GROUP BY customer_location"
            );

            logger.info("UDF demonstration completed successfully");
            return Map.of(
                    "sales_with_tax", convertToList(salesWithTax.limit(5)),
                    "sales_with_discount", convertToList(salesWithDiscount),
                    "customer_analytics", convertToList(customerAnalytics)
            );

        } catch (Exception e) {
            logger.error("Error in UDF demonstration: ", e);
            throw new RuntimeException("UDF demonstration failed", e);
        }
    }

    /**
     * Demonstrates SQL optimization techniques
     */
    public Map<String, Object> demonstrateSQLOptimization() {
        logger.info("Demonstrating SQL optimization techniques");

        try {
            setupSampleData();

            // Create larger dataset for optimization demonstration
            createLargerSampleData();

            // Broadcast joins
            Dataset<Row> smallTable = sparkSession.sql(
                    "SELECT DISTINCT customer_location, " +
                            "CASE customer_location " +
                            "  WHEN 'New York' THEN 'East' " +
                            "  WHEN 'California' THEN 'West' " +
                            "  WHEN 'Texas' THEN 'South' " +
                            "  WHEN 'Florida' THEN 'South' " +
                            "  ELSE 'Unknown' " +
                            "END as region " +
                            "FROM sales"
            );
            smallTable.createOrReplaceTempView("regions");

            // Force broadcast join
            Dataset<Row> broadcastJoin = sparkSession.sql(
                    "SELECT /*+ BROADCAST(r) */ s.*, r.region " +
                            "FROM sales s " +
                            "JOIN regions r ON s.customer_location = r.customer_location"
            );

            // Bucketing demonstration
            Dataset<Row> salesDF = sparkSession.sql("SELECT * FROM sales");
            salesDF.write()
                    .mode(SaveMode.Overwrite)
                    .bucketBy(4, "customer_id")
                    .sortBy("transaction_date")
                    .saveAsTable("bucketed_sales");

            // Partitioning
            salesDF.write()
                    .mode(SaveMode.Overwrite)
                    .partitionBy("category")
                    .parquet("/tmp/partitioned_sales");

            // Caching frequently used tables
            sparkSession.sql("CACHE TABLE sales");

            // Analyze table statistics
            sparkSession.sql("ANALYZE TABLE sales COMPUTE STATISTICS FOR ALL COLUMNS");

            // Query with statistics
            Dataset<Row> optimizedQuery = sparkSession.sql(
                    "SELECT category, COUNT(*) as count " +
                            "FROM sales " +
                            "WHERE total_amount > 100 " +
                            "GROUP BY category " +
                            "ORDER BY count DESC"
            );

            // Predicate pushdown example
            Dataset<Row> predicatePushdown = sparkSession.sql(
                    "SELECT * FROM sales " +
                            "WHERE category = 'Electronics' AND total_amount > 50"
            );

            logger.info("SQL optimization demonstration completed successfully");
            return Map.of(
                    "broadcast_join_count", broadcastJoin.count(),
                    "optimized_query_result", convertToList(optimizedQuery),
                    "predicate_pushdown_count", predicatePushdown.count(),
                    "optimization_techniques", Arrays.asList(
                            "Broadcast joins",
                            "Bucketing",
                            "Partitioning",
                            "Caching",
                            "Statistics collection",
                            "Predicate pushdown"
                    )
            );

        } catch (Exception e) {
            logger.error("Error in SQL optimization demonstration: ", e);
            throw new RuntimeException("SQL optimization demonstration failed", e);
        }
    }

    /**
     * Sets up sample data for SQL operations
     */
    private void setupSampleData() {
        // Create sample sales data directly
        List<Row> salesData = Arrays.asList(
                RowFactory.create("TXN001", "CUST001", "PROD001", "Laptop", "Electronics", 
                                1, 1200.00, 1200.00, java.sql.Timestamp.valueOf("2024-01-15 10:30:00"), 
                                35, "M", "New York", "Credit Card"),
                RowFactory.create("TXN002", "CUST002", "PROD002", "Coffee Maker", "Appliances", 
                                2, 89.99, 179.98, java.sql.Timestamp.valueOf("2024-01-15 14:15:00"), 
                                28, "F", "California", "Debit Card"),
                RowFactory.create("TXN003", "CUST003", "PROD003", "Book", "Books", 
                                3, 45.00, 135.00, java.sql.Timestamp.valueOf("2024-01-16 09:20:00"), 
                                42, "M", "Texas", "PayPal"),
                RowFactory.create("TXN004", "CUST001", "PROD004", "Mouse", "Electronics", 
                                1, 25.99, 25.99, java.sql.Timestamp.valueOf("2024-01-16 16:45:00"), 
                                35, "M", "New York", "Credit Card"),
                RowFactory.create("TXN005", "CUST004", "PROD005", "Shoes", "Sports", 
                                1, 120.00, 120.00, java.sql.Timestamp.valueOf("2024-01-17 11:00:00"), 
                                25, "F", "Florida", "Credit Card")
        );

        StructType schema = new StructType(new StructField[]{
                DataTypes.createStructField("transaction_id", DataTypes.StringType, false),
                DataTypes.createStructField("customer_id", DataTypes.StringType, false),
                DataTypes.createStructField("product_id", DataTypes.StringType, false),
                DataTypes.createStructField("product_name", DataTypes.StringType, false),
                DataTypes.createStructField("category", DataTypes.StringType, false),
                DataTypes.createStructField("quantity", DataTypes.IntegerType, false),
                DataTypes.createStructField("unit_price", DataTypes.DoubleType, false),
                DataTypes.createStructField("total_amount", DataTypes.DoubleType, false),
                DataTypes.createStructField("transaction_date", DataTypes.TimestampType, false),
                DataTypes.createStructField("customer_age", DataTypes.IntegerType, true),
                DataTypes.createStructField("customer_gender", DataTypes.StringType, true),
                DataTypes.createStructField("customer_location", DataTypes.StringType, true),
                DataTypes.createStructField("payment_method", DataTypes.StringType, true)
        });

        Dataset<Row> salesDF = sparkSession.createDataFrame(salesData, schema);
        salesDF.createOrReplaceTempView("sales");
    }

    /**
     * Creates larger sample data for optimization testing
     */
    private void createLargerSampleData() {
        // This would typically load from external data sources
        // For demo purposes, we'll create some additional synthetic data
        List<Row> additionalData = Arrays.asList(
                RowFactory.create("TXN006", "CUST005", "PROD006", "Tablet", "Electronics", 
                                1, 500.00, 500.00, java.sql.Timestamp.valueOf("2024-01-18 12:00:00"), 
                                30, "M", "California", "Credit Card"),
                RowFactory.create("TXN007", "CUST006", "PROD007", "Headphones", "Electronics", 
                                2, 150.00, 300.00, java.sql.Timestamp.valueOf("2024-01-18 15:30:00"), 
                                22, "F", "New York", "PayPal")
        );

        // This is a simplified example - in practice, you'd work with much larger datasets
    }

    /**
     * Converts DataFrame to List for JSON serialization
     */
    private List<Map<String, Object>> convertToList(Dataset<Row> df) {
        return df.toJavaRDD().map(row -> {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < row.length(); i++) {
                map.put(df.columns()[i], row.get(i));
            }
            return map;
        }).collect();
    }
}
