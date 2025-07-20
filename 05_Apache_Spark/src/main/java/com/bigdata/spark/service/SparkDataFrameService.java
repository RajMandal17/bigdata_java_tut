package com.bigdata.spark.service;

import com.bigdata.spark.model.SalesData;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.api.java.function.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.spark.sql.functions.*;

/**
 * Service class for Apache Spark DataFrame and Dataset operations
 * Demonstrates structured data processing with Spark SQL
 */
@Service
public class SparkDataFrameService {

    private static final Logger logger = LoggerFactory.getLogger(SparkDataFrameService.class);

    @Autowired
    private SparkSession sparkSession;

    /**
     * Demonstrates basic DataFrame operations
     */
    public Map<String, Object> performBasicDataFrameOperations() {
        logger.info("Performing basic DataFrame operations");

        try {
            // Create DataFrame from sample data
            Dataset<Row> salesDF = createSampleSalesDataFrame();

            // Basic operations
            long count = salesDF.count();
            String[] columns = salesDF.columns();
            StructType schema = salesDF.schema();

            // Show first few rows
            salesDF.show(5);

            // Select specific columns
            Dataset<Row> customerData = salesDF.select("customer_id", "customer_location", "total_amount");

            // Filter operations
            Dataset<Row> highValueSales = salesDF.filter(col("total_amount").gt(100));
            Dataset<Row> electronicsSales = salesDF.filter(col("category").equalTo("Electronics"));

            // Aggregations
            Dataset<Row> categoryStats = salesDF.groupBy("category")
                    .agg(
                            sum("total_amount").alias("total_sales"),
                            avg("total_amount").alias("avg_sales"),
                            count("*").alias("transaction_count"),
                            max("total_amount").alias("max_sale"),
                            min("total_amount").alias("min_sale")
                    );

            // Sort operations
            Dataset<Row> sortedByAmount = salesDF.orderBy(col("total_amount").desc());

            logger.info("Basic DataFrame operations completed successfully");
            return Map.of(
                    "total_count", count,
                    "columns", Arrays.asList(columns),
                    "high_value_sales_count", highValueSales.count(),
                    "electronics_sales_count", electronicsSales.count(),
                    "category_stats", convertToList(categoryStats),
                    "top_sales", convertToList(sortedByAmount.limit(3))
            );

        } catch (Exception e) {
            logger.error("Error in basic DataFrame operations: ", e);
            throw new RuntimeException("Basic DataFrame operations failed", e);
        }
    }

    /**
     * Demonstrates advanced DataFrame operations
     */
    public Map<String, Object> performAdvancedDataFrameOperations() {
        logger.info("Performing advanced DataFrame operations");

        try {
            Dataset<Row> salesDF = createSampleSalesDataFrame();

            // Window functions
            Dataset<Row> windowedData = salesDF
                    .withColumn("row_number",
                            row_number().over(
                                    Window.partitionBy("customer_id")
                                          .orderBy(col("total_amount").desc())
                            )
                    )
                    .withColumn("running_total",
                            sum("total_amount").over(
                                    Window.partitionBy("customer_id")
                                          .orderBy("transaction_date")
                                          .rowsBetween(Window.unboundedPreceding(), Window.currentRow())
                            )
                    );

            // Pivot operations
            Dataset<Row> pivotData = salesDF
                    .groupBy("customer_location")
                    .pivot("category")
                    .agg(sum("total_amount"));

            // Union operations
            Dataset<Row> recentSales = salesDF.filter(col("customer_age").lt(30));
            Dataset<Row> seniorSales = salesDF.filter(col("customer_age").gt(40));
            Dataset<Row> combinedSales = recentSales.union(seniorSales);

            // Join operations
            Dataset<Row> customerSummary = salesDF
                    .groupBy("customer_id", "customer_location", "customer_age")
                    .agg(
                            sum("total_amount").alias("total_spent"),
                            count("*").alias("transaction_count")
                    );

            Dataset<Row> productSummary = salesDF
                    .groupBy("product_id", "product_name", "category")
                    .agg(
                            sum("quantity").alias("total_quantity_sold"),
                            sum("total_amount").alias("total_revenue")
                    );

            // Self join example - customers who bought electronics
            Dataset<Row> electronicsCustomers = salesDF
                    .filter(col("category").equalTo("Electronics"))
                    .select("customer_id")
                    .distinct();

            Dataset<Row> electronicsCustomersPurchases = salesDF
                    .join(electronicsCustomers, "customer_id")
                    .select("customer_id", "product_name", "category", "total_amount");

            // Complex aggregations with multiple grouping
            Dataset<Row> complexAggregation = salesDF
                    .groupBy("customer_location", "payment_method")
                    .agg(
                            sum("total_amount").alias("total_sales"),
                            avg("customer_age").alias("avg_customer_age"),
                            countDistinct("customer_id").alias("unique_customers")
                    )
                    .orderBy(col("total_sales").desc());

            logger.info("Advanced DataFrame operations completed successfully");
            return Map.of(
                    "windowed_data_sample", convertToList(windowedData.limit(5)),
                    "pivot_data", convertToList(pivotData),
                    "combined_sales_count", combinedSales.count(),
                    "customer_summary", convertToList(customerSummary),
                    "product_summary", convertToList(productSummary),
                    "electronics_customers_purchases", convertToList(electronicsCustomersPurchases),
                    "complex_aggregation", convertToList(complexAggregation)
            );

        } catch (Exception e) {
            logger.error("Error in advanced DataFrame operations: ", e);
            throw new RuntimeException("Advanced DataFrame operations failed", e);
        }
    }

    /**
     * Demonstrates Dataset operations with strongly-typed APIs
     */
    public Map<String, Object> performDatasetOperations() {
        logger.info("Performing Dataset operations");

        try {
            // Create Dataset from JavaBean
            List<SalesData> salesList = createSampleSalesDataList();
            Dataset<SalesData> salesDS = sparkSession.createDataset(salesList, Encoders.bean(SalesData.class));

            // Typed transformations
            Dataset<String> customerIds = salesDS.map(
                    (MapFunction<SalesData, String>) SalesData::getCustomerId,
                    Encoders.STRING()
            );

            Dataset<BigDecimal> amounts = salesDS.map(
                    (MapFunction<SalesData, BigDecimal>) SalesData::getTotalAmount,
                    Encoders.kryo(BigDecimal.class)
            );

            // Filter with lambda
            Dataset<SalesData> highValueSales = salesDS.filter(
                    (FilterFunction<SalesData>) sale -> sale.getTotalAmount().compareTo(new BigDecimal("100")) > 0
            );

            // GroupByKey operations
            KeyValueGroupedDataset<String, SalesData> groupedByCategory = salesDS.groupByKey(
                    (MapFunction<SalesData, String>) SalesData::getCategory,
                    Encoders.STRING()
            );

            Dataset<Row> categoryTotals = groupedByCategory.agg(
                    sum(col("total_amount")).alias("total_sales")
            );

            // Reduce operations
            SalesData maxSale = salesDS.reduce(
                    (ReduceFunction<SalesData>) (sale1, sale2) ->
                            sale1.getTotalAmount().compareTo(sale2.getTotalAmount()) > 0 ? sale1 : sale2
            );

            // Convert back to DataFrame for SQL operations
            Dataset<Row> salesDF = salesDS.toDF();
            salesDF.createOrReplaceTempView("sales");

            // Register UDF (User Defined Function)
            sparkSession.udf().register("calculate_discount", 
                    (UDF2<Double, String, Double>) (amount, paymentMethod) -> {
                        if ("Credit Card".equals(paymentMethod)) {
                            return amount * 0.02; // 2% discount for credit card
                        } else if ("PayPal".equals(paymentMethod)) {
                            return amount * 0.01; // 1% discount for PayPal
                        }
                        return 0.0;
                    }, DataTypes.DoubleType);

            Dataset<Row> salesWithDiscount = sparkSession.sql(
                    "SELECT *, calculate_discount(total_amount, payment_method) as discount " +
                            "FROM sales"
            );

            logger.info("Dataset operations completed successfully");
            return Map.of(
                    "unique_customers", customerIds.distinct().count(),
                    "total_amount_sum", amounts.reduce((a, b) -> a.add(b)),
                    "high_value_sales_count", highValueSales.count(),
                    "category_totals", convertToList(categoryTotals),
                    "max_sale", maxSale.toString(),
                    "sales_with_discount", convertToList(salesWithDiscount.limit(5))
            );

        } catch (Exception e) {
            logger.error("Error in Dataset operations: ", e);
            throw new RuntimeException("Dataset operations failed", e);
        }
    }

    /**
     * Demonstrates DataFrame I/O operations
     */
    public Map<String, Object> demonstrateDataFrameIO() {
        logger.info("Demonstrating DataFrame I/O operations");

        try {
            Dataset<Row> salesDF = createSampleSalesDataFrame();

            // Write operations
            String outputPath = "/tmp/spark-output";

            // Write as Parquet
            salesDF.write()
                    .mode(SaveMode.Overwrite)
                    .option("compression", "snappy")
                    .parquet(outputPath + "/sales_parquet");

            // Write as JSON
            salesDF.write()
                    .mode(SaveMode.Overwrite)
                    .json(outputPath + "/sales_json");

            // Write as CSV with header
            salesDF.write()
                    .mode(SaveMode.Overwrite)
                    .option("header", "true")
                    .csv(outputPath + "/sales_csv");

            // Partitioned write
            salesDF.write()
                    .mode(SaveMode.Overwrite)
                    .partitionBy("category")
                    .parquet(outputPath + "/sales_partitioned");

            // Read operations
            Dataset<Row> parquetDF = sparkSession.read()
                    .parquet(outputPath + "/sales_parquet");

            Dataset<Row> jsonDF = sparkSession.read()
                    .option("multiline", "true")
                    .json(outputPath + "/sales_json");

            Dataset<Row> csvDF = sparkSession.read()
                    .option("header", "true")
                    .option("inferSchema", "true")
                    .csv(outputPath + "/sales_csv");

            // Database operations (simulated)
            Map<String, String> jdbcOptions = new HashMap<>();
            jdbcOptions.put("url", "jdbc:postgresql://localhost:5432/spark_db");
            jdbcOptions.put("dbtable", "spark_analytics.sales_data");
            jdbcOptions.put("user", "spark_user");
            jdbcOptions.put("password", "spark_password");
            jdbcOptions.put("driver", "org.postgresql.Driver");

            logger.info("DataFrame I/O operations completed successfully");
            return Map.of(
                    "original_count", salesDF.count(),
                    "parquet_count", parquetDF.count(),
                    "json_count", jsonDF.count(),
                    "csv_count", csvDF.count(),
                    "parquet_schema", parquetDF.schema().toString(),
                    "write_paths", Arrays.asList(
                            outputPath + "/sales_parquet",
                            outputPath + "/sales_json",
                            outputPath + "/sales_csv",
                            outputPath + "/sales_partitioned"
                    )
            );

        } catch (Exception e) {
            logger.error("Error in DataFrame I/O operations: ", e);
            throw new RuntimeException("DataFrame I/O operations failed", e);
        }
    }

    /**
     * Creates sample sales DataFrame
     */
    private Dataset<Row> createSampleSalesDataFrame() {
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

        return sparkSession.createDataFrame(salesData, schema);
    }

    /**
     * Creates sample sales data list for Dataset operations
     */
    private List<SalesData> createSampleSalesDataList() {
        return Arrays.asList(
                new SalesData("TXN001", "CUST001", "PROD001", "Laptop", "Electronics", 
                             1, new BigDecimal("1200.00"), new BigDecimal("1200.00"), 
                             LocalDateTime.of(2024, 1, 15, 10, 30), 35, "M", "New York", "Credit Card"),
                new SalesData("TXN002", "CUST002", "PROD002", "Coffee Maker", "Appliances", 
                             2, new BigDecimal("89.99"), new BigDecimal("179.98"), 
                             LocalDateTime.of(2024, 1, 15, 14, 15), 28, "F", "California", "Debit Card"),
                new SalesData("TXN003", "CUST003", "PROD003", "Book", "Books", 
                             3, new BigDecimal("45.00"), new BigDecimal("135.00"), 
                             LocalDateTime.of(2024, 1, 16, 9, 20), 42, "M", "Texas", "PayPal"),
                new SalesData("TXN004", "CUST001", "PROD004", "Mouse", "Electronics", 
                             1, new BigDecimal("25.99"), new BigDecimal("25.99"), 
                             LocalDateTime.of(2024, 1, 16, 16, 45), 35, "M", "New York", "Credit Card"),
                new SalesData("TXN005", "CUST004", "PROD005", "Shoes", "Sports", 
                             1, new BigDecimal("120.00"), new BigDecimal("120.00"), 
                             LocalDateTime.of(2024, 1, 17, 11, 0), 25, "F", "Florida", "Credit Card")
        );
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
