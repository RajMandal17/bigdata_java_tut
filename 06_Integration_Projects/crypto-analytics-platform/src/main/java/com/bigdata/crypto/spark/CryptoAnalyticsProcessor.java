package com.bigdata.crypto.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static org.apache.spark.sql.functions.*;

@Component
public class CryptoAnalyticsProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(CryptoAnalyticsProcessor.class);
    
    @Autowired
    private SparkSession sparkSession;
    
    @PostConstruct
    public void startCryptoStreamProcessing() {
        logger.info("Starting crypto stream processing...");
        
        try {
            processTradingEvents();
            processOrderBookEvents();
            detectMarketAnomalies();
            monitorTradingVolume();
            
        } catch (Exception e) {
            logger.error("Error starting crypto stream processing", e);
        }
    }
    
    /**
     * Process real-time trading events from Gitbitex
     */
    private void processTradingEvents() {
        logger.info("Starting trading events processing...");
        
        try {
            Dataset<Row> tradeEvents = sparkSession
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "gitbitex-trades")
                .option("startingOffsets", "latest")
                .load()
                .select(from_json(col("value").cast("string"), getTradeEventSchema()).alias("trade"))
                .select("trade.*")
                .withColumn("timestamp", to_timestamp(col("timestamp")));
            
            // Real-time trading volume and price analytics
            Dataset<Row> tradingMetrics = tradeEvents
                .withWatermark("timestamp", "30 seconds")
                .groupBy(
                    window(col("timestamp"), "1 minute"),
                    col("symbol")
                )
                .agg(
                    count("*").alias("tradeCount"),
                    sum("quantity").alias("volume"),
                    sum(col("quantity").multiply(col("price"))).alias("volumeUSDT"),
                    first("price").alias("openPrice"),
                    last("price").alias("closePrice"),
                    max("price").alias("highPrice"),
                    min("price").alias("lowPrice"),
                    stddev("price").alias("priceVolatility"),
                    avg("price").alias("avgPrice")
                );
            
            // Calculate OHLC candlesticks
            Dataset<Row> candlesticks = tradingMetrics
                .withColumn("priceChange", col("closePrice").minus(col("openPrice")))
                .withColumn("priceChangePercent", 
                    when(col("openPrice").gt(0), 
                        col("priceChange").divide(col("openPrice")).multiply(100))
                    .otherwise(0))
                .withColumn("windowStart", col("window.start"))
                .withColumn("windowEnd", col("window.end"))
                .drop("window");
            
            // Write to Redis for real-time dashboard
            StreamingQuery tradingMetricsQuery = candlesticks
                .writeStream()
                .outputMode("update")
                .foreachBatch((batchDF, batchId) -> {
                    logger.info("Processing trading metrics batch: {}", batchId);
                    writeToRedis(batchDF, "trading_metrics");
                    updateInfluxDB(batchDF, "candlesticks");
                })
                .trigger(Trigger.ProcessingTime("5 seconds"))
                .start();
            
            // Real-time top gainers/losers
            Dataset<Row> priceMovers = candlesticks
                .filter(col("priceChangePercent").isNotNull())
                .withWatermark("windowStart", "5 minutes")
                .groupBy(window(col("windowStart"), "15 minutes"))
                .agg(
                    collect_list(
                        struct(col("symbol"), col("priceChangePercent"), col("volumeUSDT"), col("closePrice"))
                            .alias("symbolData")
                    ).alias("symbols")
                )
                .select(
                    col("window"),
                    expr("filter(symbols, x -> x.priceChangePercent > 0)").alias("gainers"),
                    expr("filter(symbols, x -> x.priceChangePercent < 0)").alias("losers")
                )
                .select(
                    col("window"),
                    expr("slice(sort_array(gainers, false), 1, 10)").alias("topGainers"),
                    expr("slice(sort_array(losers, true), 1, 10)").alias("topLosers")
                );
            
            priceMovers
                .writeStream()
                .outputMode("update")
                .foreachBatch((batchDF, batchId) -> {
                    publishTopMovers(batchDF, batchId);
                })
                .trigger(Trigger.ProcessingTime("15 seconds"))
                .start();
                
        } catch (Exception e) {
            logger.error("Error in trading events processing", e);
        }
    }
    
    /**
     * Process order book events for market depth analysis
     */
    private void processOrderBookEvents() {
        logger.info("Starting order book events processing...");
        
        try {
            Dataset<Row> orderEvents = sparkSession
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "gitbitex-orders")
                .option("startingOffsets", "latest")
                .load()
                .select(from_json(col("value").cast("string"), getOrderEventSchema()).alias("order"))
                .select("order.*")
                .withColumn("timestamp", to_timestamp(col("timestamp")));
            
            // Real-time order book imbalance
            Dataset<Row> orderBookImbalance = orderEvents
                .filter(col("status").equalTo("OPEN"))
                .withWatermark("timestamp", "1 minute")
                .groupBy(
                    window(col("timestamp"), "30 seconds"),
                    col("symbol"),
                    col("side")
                )
                .agg(
                    sum("quantity").alias("totalQuantity"),
                    sum(col("quantity").multiply(col("price"))).alias("totalValue"),
                    count("*").alias("orderCount"),
                    avg("price").alias("avgPrice")
                )
                .groupBy(col("window"), col("symbol"))
                .pivot("side")
                .agg(
                    first("totalQuantity").alias("quantity"),
                    first("totalValue").alias("value"),
                    first("orderCount").alias("count"),
                    first("avgPrice").alias("avgPrice")
                )
                .withColumn("imbalanceRatio", 
                    when(col("SELL_quantity").gt(0), 
                        col("BUY_quantity").divide(col("SELL_quantity")))
                    .otherwise(999))
                .withColumn("spreadPressure",
                    coalesce(col("BUY_value"), lit(0)).minus(coalesce(col("SELL_value"), lit(0))))
                .withColumn("windowStart", col("window.start"))
                .drop("window");
            
            // Detect large orders (whale activity)
            Dataset<Row> whaleOrders = orderEvents
                .filter(col("quantity").gt(1000).or(
                    col("quantity").multiply(col("price")).gt(100000)
                ))
                .withColumn("orderSizeUSD", col("quantity").multiply(col("price")))
                .withColumn("marketImpact", 
                    when(col("side").equalTo("BUY"), col("orderSizeUSD").multiply(0.001))
                    .otherwise(col("orderSizeUSD").multiply(-0.001))
                )
                .withColumn("whaleType",
                    when(col("orderSizeUSD").gt(1000000), "MEGA_WHALE")
                    .when(col("orderSizeUSD").gt(500000), "LARGE_WHALE")
                    .otherwise("WHALE")
                );
            
            // Write whale activity to MongoDB
            whaleOrders
                .writeStream()
                .outputMode("append")
                .foreachBatch((batchDF, batchId) -> {
                    logger.info("Processing whale orders batch: {}", batchId);
                    writeToMongoDB(batchDF, "whale_activity");
                })
                .trigger(Trigger.ProcessingTime("10 seconds"))
                .start();
                
            // Write order book imbalance to Redis
            orderBookImbalance
                .writeStream()
                .outputMode("update")
                .foreachBatch((batchDF, batchId) -> {
                    writeToRedis(batchDF, "order_imbalance");
                })
                .trigger(Trigger.ProcessingTime("30 seconds"))
                .start();
                
        } catch (Exception e) {
            logger.error("Error in order book events processing", e);
        }
    }
    
    /**
     * Detect market manipulation and anomalies
     */
    private void detectMarketAnomalies() {
        logger.info("Starting market anomaly detection...");
        
        try {
            Dataset<Row> tradeEvents = sparkSession
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "gitbitex-trades")
                .option("startingOffsets", "latest")
                .load()
                .select(from_json(col("value").cast("string"), getTradeEventSchema()).alias("trade"))
                .select("trade.*")
                .withColumn("timestamp", to_timestamp(col("timestamp")));
            
            // Wash trading detection
            Dataset<Row> suspiciousPatterns = tradeEvents
                .withWatermark("timestamp", "10 minutes")
                .groupBy(
                    window(col("timestamp"), "5 minutes"),
                    col("symbol"),
                    col("buyOrderId"),
                    col("sellOrderId")
                )
                .agg(
                    count("*").alias("tradeCount"),
                    countDistinct("price").alias("uniquePrices"),
                    sum("quantity").alias("totalVolume"),
                    stddev("price").alias("priceStdDev")
                )
                .filter(
                    col("tradeCount").gt(50).and(
                    col("uniquePrices").lt(3)).and(
                    col("totalVolume").gt(10000))
                )
                .withColumn("manipulationScore", 
                    col("tradeCount").divide(col("uniquePrices")).multiply(100))
                .withColumn("suspicionLevel",
                    when(col("manipulationScore").gt(500), "HIGH")
                    .when(col("manipulationScore").gt(200), "MEDIUM")
                    .otherwise("LOW"))
                .filter(col("suspicionLevel").isin("HIGH", "MEDIUM"));
            
            // Pump and dump detection
            Dataset<Row> priceManipulation = tradeEvents
                .withWatermark("timestamp", "15 minutes")
                .groupBy(
                    window(col("timestamp"), "10 minutes"),
                    col("symbol")
                )
                .agg(
                    first("price").alias("startPrice"),
                    last("price").alias("endPrice"),
                    max("price").alias("peakPrice"),
                    sum("quantity").alias("volume"),
                    count("*").alias("tradeCount"),
                    stddev("price").alias("priceVolatility")
                )
                .withColumn("priceIncrease", 
                    when(col("startPrice").gt(0),
                        col("peakPrice").divide(col("startPrice")).minus(1).multiply(100))
                    .otherwise(0))
                .withColumn("priceDrop",
                    when(col("endPrice").gt(0),
                        col("peakPrice").divide(col("endPrice")).minus(1).multiply(100))
                    .otherwise(0))
                .filter(
                    col("priceIncrease").gt(20).and(
                    col("priceDrop").gt(15)).and(
                    col("volume").gt(5000))
                )
                .withColumn("pumpDumpScore",
                    col("priceIncrease").plus(col("priceDrop")).divide(2));
            
            // Send anomaly alerts
            suspiciousPatterns
                .union(priceManipulation.select(suspiciousPatterns.columns.map(col): _*))
                .writeStream()
                .outputMode("update")
                .foreachBatch((batchDF, batchId) -> {
                    sendAnomalyAlerts(batchDF, batchId);
                })
                .trigger(Trigger.ProcessingTime("30 seconds"))
                .start();
                
        } catch (Exception e) {
            logger.error("Error in market anomaly detection", e);
        }
    }
    
    /**
     * Monitor trading volume patterns
     */
    private void monitorTradingVolume() {
        logger.info("Starting trading volume monitoring...");
        
        try {
            Dataset<Row> tradeEvents = sparkSession
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "gitbitex-trades")
                .option("startingOffsets", "latest")
                .load()
                .select(from_json(col("value").cast("string"), getTradeEventSchema()).alias("trade"))
                .select("trade.*")
                .withColumn("timestamp", to_timestamp(col("timestamp")));
            
            // Volume surge detection
            Dataset<Row> volumeMetrics = tradeEvents
                .withWatermark("timestamp", "5 minutes")
                .groupBy(
                    window(col("timestamp"), "1 minute"),
                    col("symbol")
                )
                .agg(
                    sum(col("quantity").multiply(col("price"))).alias("volumeUSDT"),
                    count("*").alias("tradeCount"),
                    avg(col("quantity").multiply(col("price"))).alias("avgTradeSize")
                )
                .withColumn("volumeCategory",
                    when(col("volumeUSDT").gt(1000000), "VERY_HIGH")
                    .when(col("volumeUSDT").gt(500000), "HIGH")
                    .when(col("volumeUSDT").gt(100000), "MEDIUM")
                    .otherwise("LOW"))
                .withColumn("windowStart", col("window.start"))
                .drop("window");
            
            // Store volume metrics
            volumeMetrics
                .writeStream()
                .outputMode("update")
                .foreachBatch((batchDF, batchId) -> {
                    writeToInfluxDB(batchDF, "volume_metrics");
                    updateVolumeAlerts(batchDF, batchId);
                })
                .trigger(Trigger.ProcessingTime("60 seconds"))
                .start();
                
        } catch (Exception e) {
            logger.error("Error in trading volume monitoring", e);
        }
    }
    
    // Helper methods for data schemas
    private org.apache.spark.sql.types.StructType getTradeEventSchema() {
        return new org.apache.spark.sql.types.StructType()
            .add("tradeId", org.apache.spark.sql.types.DataTypes.StringType)
            .add("symbol", org.apache.spark.sql.types.DataTypes.StringType)
            .add("side", org.apache.spark.sql.types.DataTypes.StringType)
            .add("quantity", org.apache.spark.sql.types.DataTypes.createDecimalType(18, 8))
            .add("price", org.apache.spark.sql.types.DataTypes.createDecimalType(18, 8))
            .add("timestamp", org.apache.spark.sql.types.DataTypes.TimestampType)
            .add("buyOrderId", org.apache.spark.sql.types.DataTypes.StringType)
            .add("sellOrderId", org.apache.spark.sql.types.DataTypes.StringType)
            .add("userId", org.apache.spark.sql.types.DataTypes.StringType);
    }
    
    private org.apache.spark.sql.types.StructType getOrderEventSchema() {
        return new org.apache.spark.sql.types.StructType()
            .add("orderId", org.apache.spark.sql.types.DataTypes.StringType)
            .add("symbol", org.apache.spark.sql.types.DataTypes.StringType)
            .add("side", org.apache.spark.sql.types.DataTypes.StringType)
            .add("type", org.apache.spark.sql.types.DataTypes.StringType)
            .add("status", org.apache.spark.sql.types.DataTypes.StringType)
            .add("quantity", org.apache.spark.sql.types.DataTypes.createDecimalType(18, 8))
            .add("price", org.apache.spark.sql.types.DataTypes.createDecimalType(18, 8))
            .add("timestamp", org.apache.spark.sql.types.DataTypes.TimestampType)
            .add("userId", org.apache.spark.sql.types.DataTypes.StringType);
    }
    
    // Helper methods for data output
    private void writeToRedis(Dataset<Row> dataFrame, String keyPrefix) {
        try {
            dataFrame.collect(); // Simple implementation - in production, use Redis connector
            logger.debug("Written data to Redis with prefix: {}", keyPrefix);
        } catch (Exception e) {
            logger.error("Error writing to Redis", e);
        }
    }
    
    private void updateInfluxDB(Dataset<Row> dataFrame, String measurement) {
        try {
            dataFrame.collect(); // Simple implementation - in production, use InfluxDB connector
            logger.debug("Updated InfluxDB measurement: {}", measurement);
        } catch (Exception e) {
            logger.error("Error updating InfluxDB", e);
        }
    }
    
    private void writeToMongoDB(Dataset<Row> dataFrame, String collection) {
        try {
            dataFrame.collect(); // Simple implementation - in production, use MongoDB connector
            logger.debug("Written data to MongoDB collection: {}", collection);
        } catch (Exception e) {
            logger.error("Error writing to MongoDB", e);
        }
    }
    
    private void publishTopMovers(Dataset<Row> dataFrame, long batchId) {
        try {
            logger.info("Publishing top movers for batch: {}", batchId);
            dataFrame.show(false);
        } catch (Exception e) {
            logger.error("Error publishing top movers", e);
        }
    }
    
    private void sendAnomalyAlerts(Dataset<Row> dataFrame, long batchId) {
        try {
            logger.warn("Sending anomaly alerts for batch: {}", batchId);
            dataFrame.show(false);
        } catch (Exception e) {
            logger.error("Error sending anomaly alerts", e);
        }
    }
    
    private void updateVolumeAlerts(Dataset<Row> dataFrame, long batchId) {
        try {
            logger.info("Updating volume alerts for batch: {}", batchId);
            dataFrame.filter(col("volumeCategory").isin("VERY_HIGH", "HIGH")).show(false);
        } catch (Exception e) {
            logger.error("Error updating volume alerts", e);
        }
    }
}
