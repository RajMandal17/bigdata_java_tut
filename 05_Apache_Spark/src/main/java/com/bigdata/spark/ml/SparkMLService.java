package com.bigdata.spark.ml;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.*;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.ml.evaluation.*;
import org.apache.spark.ml.feature.*;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.ml.tuning.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.spark.sql.functions.*;

/**
 * Service class for Apache Spark Machine Learning operations
 * Demonstrates MLlib features including classification, clustering, and recommendation systems
 */
@Service
public class SparkMLService {

    private static final Logger logger = LoggerFactory.getLogger(SparkMLService.class);

    @Autowired
    private SparkSession sparkSession;

    /**
     * Demonstrates feature engineering pipeline
     */
    public Map<String, Object> performFeatureEngineering() {
        logger.info("Performing feature engineering");

        try {
            // Create sample data for feature engineering
            Dataset<Row> rawData = createSampleMLData();

            // String indexing for categorical variables
            StringIndexer categoryIndexer = new StringIndexer()
                    .setInputCol("category")
                    .setOutputCol("category_index")
                    .setHandleInvalid("keep");

            StringIndexer genderIndexer = new StringIndexer()
                    .setInputCol("customer_gender")
                    .setOutputCol("gender_index")
                    .setHandleInvalid("keep");

            StringIndexer paymentIndexer = new StringIndexer()
                    .setInputCol("payment_method")
                    .setOutputCol("payment_index")
                    .setHandleInvalid("keep");

            // One-hot encoding
            OneHotEncoder categoryEncoder = new OneHotEncoder()
                    .setInputCol("category_index")
                    .setOutputCol("category_vector");

            OneHotEncoder genderEncoder = new OneHotEncoder()
                    .setInputCol("gender_index")
                    .setOutputCol("gender_vector");

            OneHotEncoder paymentEncoder = new OneHotEncoder()
                    .setInputCol("payment_index")
                    .setOutputCol("payment_vector");

            // Numeric feature transformations
            Bucketizer ageEncoder = new Bucketizer()
                    .setInputCol("customer_age")
                    .setOutputCol("age_bucket")
                    .setSplits(new double[]{0, 25, 35, 50, 65, Double.POSITIVE_INFINITY});

            // Create additional features
            Dataset<Row> enrichedData = rawData
                    .withColumn("amount_log", log1p(col("total_amount")))
                    .withColumn("quantity_squared", pow(col("quantity"), 2))
                    .withColumn("unit_price_log", log1p(col("unit_price")))
                    .withColumn("is_weekend", 
                            when(dayofweek(col("transaction_date")).isin(1, 7), 1.0).otherwise(0.0))
                    .withColumn("hour_of_day", hour(col("transaction_date")))
                    .withColumn("day_of_week", dayofweek(col("transaction_date")));

            // Vector assembler for numeric features
            VectorAssembler numericAssembler = new VectorAssembler()
                    .setInputCols(new String[]{"total_amount", "quantity", "unit_price", 
                                             "customer_age", "amount_log", "quantity_squared", 
                                             "unit_price_log", "is_weekend", "hour_of_day", "day_of_week"})
                    .setOutputCol("numeric_features");

            // Standard scaler for numeric features
            StandardScaler scaler = new StandardScaler()
                    .setInputCol("numeric_features")
                    .setOutputCol("scaled_numeric_features");

            // Final feature vector assembler
            VectorAssembler finalAssembler = new VectorAssembler()
                    .setInputCols(new String[]{"scaled_numeric_features", "category_vector", 
                                             "gender_vector", "payment_vector", "age_bucket"})
                    .setOutputCol("features");

            // Create and fit pipeline
            Pipeline featurePipeline = new Pipeline()
                    .setStages(new PipelineStage[]{
                            categoryIndexer, genderIndexer, paymentIndexer,
                            categoryEncoder, genderEncoder, paymentEncoder,
                            ageEncoder, numericAssembler, scaler, finalAssembler
                    });

            PipelineModel featureModel = featurePipeline.fit(enrichedData);
            Dataset<Row> featuredData = featureModel.transform(enrichedData);

            logger.info("Feature engineering completed successfully");
            return Map.of(
                    "original_columns", rawData.columns().length,
                    "featured_columns", featuredData.columns().length,
                    "sample_features", convertToList(featuredData.select("features").limit(5)),
                    "feature_pipeline_stages", featurePipeline.getStages().length
            );

        } catch (Exception e) {
            logger.error("Error in feature engineering: ", e);
            throw new RuntimeException("Feature engineering failed", e);
        }
    }

    /**
     * Demonstrates binary classification for fraud detection
     */
    public Map<String, Object> performFraudDetection() {
        logger.info("Performing fraud detection with ML");

        try {
            // Create fraud detection dataset
            Dataset<Row> fraudData = createFraudDetectionData();

            // Feature engineering for fraud detection
            VectorAssembler assembler = new VectorAssembler()
                    .setInputCols(new String[]{"amount", "transaction_count_1h", "location_changes", 
                                             "payment_method_changes", "time_since_last_txn"})
                    .setOutputCol("features");

            StandardScaler scaler = new StandardScaler()
                    .setInputCol("features")
                    .setOutputCol("scaled_features");

            // Split data
            Dataset<Row>[] splits = fraudData.randomSplit(new double[]{0.8, 0.2}, 42);
            Dataset<Row> trainData = splits[0];
            Dataset<Row> testData = splits[1];

            // Random Forest classifier
            RandomForestClassifier rf = new RandomForestClassifier()
                    .setLabelCol("is_fraud")
                    .setFeaturesCol("scaled_features")
                    .setNumTrees(100)
                    .setMaxDepth(10)
                    .setSubsamplingRate(0.8);

            // Logistic Regression
            LogisticRegression lr = new LogisticRegression()
                    .setLabelCol("is_fraud")
                    .setFeaturesCol("scaled_features")
                    .setRegParam(0.01);

            // Gradient Boosted Trees
            GBTClassifier gbt = new GBTClassifier()
                    .setLabelCol("is_fraud")
                    .setFeaturesCol("scaled_features")
                    .setMaxIter(20);

            // Create pipelines
            Pipeline rfPipeline = new Pipeline().setStages(new PipelineStage[]{assembler, scaler, rf});
            Pipeline lrPipeline = new Pipeline().setStages(new PipelineStage[]{assembler, scaler, lr});
            Pipeline gbtPipeline = new Pipeline().setStages(new PipelineStage[]{assembler, scaler, gbt});

            // Train models
            PipelineModel rfModel = rfPipeline.fit(trainData);
            PipelineModel lrModel = lrPipeline.fit(trainData);
            PipelineModel gbtModel = gbtPipeline.fit(trainData);

            // Make predictions
            Dataset<Row> rfPredictions = rfModel.transform(testData);
            Dataset<Row> lrPredictions = lrModel.transform(testData);
            Dataset<Row> gbtPredictions = gbtModel.transform(testData);

            // Evaluate models
            BinaryClassificationEvaluator evaluator = new BinaryClassificationEvaluator()
                    .setLabelCol("is_fraud")
                    .setRawPredictionCol("rawPrediction")
                    .setMetricName("areaUnderROC");

            MulticlassClassificationEvaluator accuracyEvaluator = 
                    new MulticlassClassificationEvaluator()
                            .setLabelCol("is_fraud")
                            .setPredictionCol("prediction")
                            .setMetricName("accuracy");

            double rfAUC = evaluator.evaluate(rfPredictions);
            double lrAUC = evaluator.evaluate(lrPredictions);
            double gbtAUC = evaluator.evaluate(gbtPredictions);

            double rfAccuracy = accuracyEvaluator.evaluate(rfPredictions);
            double lrAccuracy = accuracyEvaluator.evaluate(lrPredictions);
            double gbtAccuracy = accuracyEvaluator.evaluate(gbtPredictions);

            // Feature importance (Random Forest)
            RandomForestClassificationModel rfMLModel = 
                    (RandomForestClassificationModel) rfModel.stages()[2];
            Vector featureImportances = rfMLModel.featureImportances();

            logger.info("Fraud detection ML completed successfully");
            return Map.of(
                    "random_forest_auc", rfAUC,
                    "logistic_regression_auc", lrAUC,
                    "gbt_auc", gbtAUC,
                    "random_forest_accuracy", rfAccuracy,
                    "logistic_regression_accuracy", lrAccuracy,
                    "gbt_accuracy", gbtAccuracy,
                    "feature_importances", featureImportances.toArray(),
                    "best_model", getBestModel(rfAUC, lrAUC, gbtAUC),
                    "test_predictions_sample", convertToList(rfPredictions.select("is_fraud", "prediction", "probability").limit(10))
            );

        } catch (Exception e) {
            logger.error("Error in fraud detection ML: ", e);
            throw new RuntimeException("Fraud detection ML failed", e);
        }
    }

    /**
     * Demonstrates customer segmentation using clustering
     */
    public Map<String, Object> performCustomerSegmentation() {
        logger.info("Performing customer segmentation");

        try {
            // Create customer features dataset
            Dataset<Row> customerData = createCustomerSegmentationData();

            // Feature engineering
            VectorAssembler assembler = new VectorAssembler()
                    .setInputCols(new String[]{"total_spent", "transaction_count", "avg_transaction_amount", 
                                             "days_since_last_purchase", "unique_categories", "avg_days_between_purchases"})
                    .setOutputCol("features");

            StandardScaler scaler = new StandardScaler()
                    .setInputCol("features")
                    .setOutputCol("scaled_features");

            // K-means clustering
            KMeans kmeans = new KMeans()
                    .setK(5)
                    .setFeaturesCol("scaled_features")
                    .setPredictionCol("cluster")
                    .setSeed(42);

            // Create pipeline
            Pipeline pipeline = new Pipeline()
                    .setStages(new PipelineStage[]{assembler, scaler, kmeans});

            // Fit model
            PipelineModel model = pipeline.fit(customerData);
            Dataset<Row> clusteredData = model.transform(customerData);

            // Analyze clusters
            Dataset<Row> clusterAnalysis = clusteredData
                    .groupBy("cluster")
                    .agg(
                            count("*").alias("customer_count"),
                            avg("total_spent").alias("avg_total_spent"),
                            avg("transaction_count").alias("avg_transaction_count"),
                            avg("avg_transaction_amount").alias("avg_transaction_amount"),
                            avg("days_since_last_purchase").alias("avg_days_since_last"),
                            avg("unique_categories").alias("avg_unique_categories")
                    )
                    .orderBy("cluster");

            // Get cluster centers
            KMeansModel kmeansModel = (KMeansModel) model.stages()[2];
            Vector[] clusterCenters = kmeansModel.clusterCenters();

            // Silhouette analysis
            ClusteringEvaluator evaluator = new ClusteringEvaluator()
                    .setFeaturesCol("scaled_features")
                    .setPredictionCol("cluster");

            double silhouette = evaluator.evaluate(clusteredData);

            logger.info("Customer segmentation completed successfully");
            return Map.of(
                    "cluster_analysis", convertToList(clusterAnalysis),
                    "silhouette_score", silhouette,
                    "cluster_centers_count", clusterCenters.length,
                    "customer_distribution", convertToList(
                            clusteredData.groupBy("cluster").count().orderBy("cluster")
                    ),
                    "sample_clustered_customers", convertToList(
                            clusteredData.select("customer_id", "cluster", "total_spent", "transaction_count").limit(10)
                    )
            );

        } catch (Exception e) {
            logger.error("Error in customer segmentation: ", e);
            throw new RuntimeException("Customer segmentation failed", e);
        }
    }

    /**
     * Demonstrates recommendation system using collaborative filtering
     */
    public Map<String, Object> buildRecommendationSystem() {
        logger.info("Building recommendation system");

        try {
            // Create ratings dataset
            Dataset<Row> ratingsData = createRatingsData();

            // Split data
            Dataset<Row>[] splits = ratingsData.randomSplit(new double[]{0.8, 0.2}, 42);
            Dataset<Row> trainData = splits[0];
            Dataset<Row> testData = splits[1];

            // ALS (Alternating Least Squares) for collaborative filtering
            ALS als = new ALS()
                    .setMaxIter(10)
                    .setRegParam(0.1)
                    .setUserCol("customer_id")
                    .setItemCol("product_id")
                    .setRatingCol("rating")
                    .setColdStartStrategy("drop");

            // Hyperparameter tuning
            ParamGridBuilder paramGrid = new ParamGridBuilder()
                    .addGrid(als.regParam(), new double[]{0.01, 0.1, 0.5})
                    .addGrid(als.rank(), new int[]{10, 20, 50});

            RegressionEvaluator evaluator = new RegressionEvaluator()
                    .setMetricName("rmse")
                    .setLabelCol("rating")
                    .setPredictionCol("prediction");

            CrossValidator cv = new CrossValidator()
                    .setEstimator(als)
                    .setEvaluator(evaluator)
                    .setEstimatorParamMaps(paramGrid.build())
                    .setNumFolds(3);

            // Train model
            CrossValidatorModel cvModel = cv.fit(trainData);
            ALSModel alsModel = (ALSModel) cvModel.bestModel();

            // Make predictions
            Dataset<Row> predictions = alsModel.transform(testData);
            double rmse = evaluator.evaluate(predictions);

            // Generate recommendations
            Dataset<Row> userRecs = alsModel.recommendForAllUsers(10);
            Dataset<Row> itemRecs = alsModel.recommendForAllItems(10);

            // Cold start handling - recommendations for new users
            Dataset<Row> newUsers = sparkSession.createDataFrame(
                    java.util.Arrays.asList(
                            org.apache.spark.sql.RowFactory.create(999),
                            org.apache.spark.sql.RowFactory.create(1000)
                    ),
                    org.apache.spark.sql.types.DataTypes.createStructType(new org.apache.spark.sql.types.StructField[]{
                            org.apache.spark.sql.types.DataTypes.createStructField("customer_id", org.apache.spark.sql.types.DataTypes.IntegerType, false)
                    })
            );

            Dataset<Row> newUserRecs = alsModel.recommendForUserSubset(newUsers, 5);

            logger.info("Recommendation system built successfully");
            return Map.of(
                    "rmse", rmse,
                    "best_rank", alsModel.rank(),
                    "user_recommendations_sample", convertToList(userRecs.limit(5)),
                    "item_recommendations_sample", convertToList(itemRecs.limit(5)),
                    "new_user_recommendations", convertToList(newUserRecs),
                    "training_data_count", trainData.count(),
                    "test_data_count", testData.count()
            );

        } catch (Exception e) {
            logger.error("Error building recommendation system: ", e);
            throw new RuntimeException("Recommendation system building failed", e);
        }
    }

    /**
     * Demonstrates regression for sales prediction
     */
    public Map<String, Object> performSalesPrediction() {
        logger.info("Performing sales prediction");

        try {
            // Create sales prediction dataset
            Dataset<Row> salesData = createSalesPredictionData();

            // Feature engineering
            VectorAssembler assembler = new VectorAssembler()
                    .setInputCols(new String[]{"historical_avg", "seasonality_factor", "promotion_active", 
                                             "competitor_price_ratio", "inventory_level", "day_of_week", "month"})
                    .setOutputCol("features");

            StandardScaler scaler = new StandardScaler()
                    .setInputCol("features")
                    .setOutputCol("scaled_features");

            // Split data
            Dataset<Row>[] splits = salesData.randomSplit(new double[]{0.8, 0.2}, 42);
            Dataset<Row> trainData = splits[0];
            Dataset<Row> testData = splits[1];

            // Linear Regression
            LinearRegression lr = new LinearRegression()
                    .setLabelCol("sales_amount")
                    .setFeaturesCol("scaled_features")
                    .setRegParam(0.1)
                    .setElasticNetParam(0.5);

            // Create pipeline
            Pipeline pipeline = new Pipeline()
                    .setStages(new PipelineStage[]{assembler, scaler, lr});

            // Train model
            PipelineModel model = pipeline.fit(trainData);
            Dataset<Row> predictions = model.transform(testData);

            // Evaluate model
            RegressionEvaluator evaluator = new RegressionEvaluator()
                    .setLabelCol("sales_amount")
                    .setPredictionCol("prediction");

            double rmse = evaluator.setMetricName("rmse").evaluate(predictions);
            double mae = evaluator.setMetricName("mae").evaluate(predictions);
            double r2 = evaluator.setMetricName("r2").evaluate(predictions);

            // Model summary
            LinearRegressionModel lrModel = (LinearRegressionModel) model.stages()[2];
            
            logger.info("Sales prediction completed successfully");
            return Map.of(
                    "rmse", rmse,
                    "mae", mae,
                    "r2", r2,
                    "coefficients", lrModel.coefficients().toArray(),
                    "intercept", lrModel.intercept(),
                    "predictions_sample", convertToList(
                            predictions.select("sales_amount", "prediction").limit(10)
                    ),
                    "model_summary", Map.of(
                            "total_iterations", lrModel.summary().totalIterations(),
                            "training_rmse", lrModel.summary().rootMeanSquaredError()
                    )
            );

        } catch (Exception e) {
            logger.error("Error in sales prediction: ", e);
            throw new RuntimeException("Sales prediction failed", e);
        }
    }

    // Helper methods for creating sample datasets

    private Dataset<Row> createSampleMLData() {
        // This would typically load from your actual data source
        return sparkSession.sql("SELECT * FROM sales LIMIT 1000");
    }

    private Dataset<Row> createFraudDetectionData() {
        // Create synthetic fraud detection data
        return sparkSession.sql(
                "SELECT " +
                        "CASE WHEN rand() < 0.05 THEN 1 ELSE 0 END as is_fraud, " +
                        "rand() * 10000 as amount, " +
                        "CAST(rand() * 20 AS INT) as transaction_count_1h, " +
                        "CAST(rand() * 5 AS INT) as location_changes, " +
                        "CAST(rand() * 3 AS INT) as payment_method_changes, " +
                        "rand() * 1440 as time_since_last_txn " +
                        "FROM range(1000)"
        );
    }

    private Dataset<Row> createCustomerSegmentationData() {
        return sparkSession.sql(
                "SELECT " +
                        "CAST(id AS STRING) as customer_id, " +
                        "rand() * 5000 + 100 as total_spent, " +
                        "CAST(rand() * 50 + 1 AS INT) as transaction_count, " +
                        "rand() * 200 + 10 as avg_transaction_amount, " +
                        "CAST(rand() * 365 AS INT) as days_since_last_purchase, " +
                        "CAST(rand() * 10 + 1 AS INT) as unique_categories, " +
                        "rand() * 30 + 1 as avg_days_between_purchases " +
                        "FROM range(1000)"
        );
    }

    private Dataset<Row> createRatingsData() {
        return sparkSession.sql(
                "SELECT " +
                        "CAST((id % 100) + 1 AS INT) as customer_id, " +
                        "CAST((id % 50) + 1 AS INT) as product_id, " +
                        "CAST(rand() * 4 + 1 AS FLOAT) as rating " +
                        "FROM range(5000)"
        );
    }

    private Dataset<Row> createSalesPredictionData() {
        return sparkSession.sql(
                "SELECT " +
                        "rand() * 1000 + 100 as sales_amount, " +
                        "rand() * 800 + 50 as historical_avg, " +
                        "rand() * 2 + 0.5 as seasonality_factor, " +
                        "CASE WHEN rand() < 0.3 THEN 1.0 ELSE 0.0 END as promotion_active, " +
                        "rand() * 0.5 + 0.8 as competitor_price_ratio, " +
                        "rand() * 1000 + 100 as inventory_level, " +
                        "CAST(rand() * 7 + 1 AS INT) as day_of_week, " +
                        "CAST(rand() * 12 + 1 AS INT) as month " +
                        "FROM range(1000)"
        );
    }

    private String getBestModel(double rfAUC, double lrAUC, double gbtAUC) {
        if (rfAUC >= lrAUC && rfAUC >= gbtAUC) return "Random Forest";
        else if (lrAUC >= gbtAUC) return "Logistic Regression";
        else return "Gradient Boosted Trees";
    }

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
