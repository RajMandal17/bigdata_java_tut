#!/bin/bash

# Create sample data directory
mkdir -p /opt/spark/sample-data

# Create sample CSV data for Spark processing
cat > /opt/spark/sample-data/users.csv << 'EOF'
user_id,name,email,age,registration_date,city,country
1,John Doe,john.doe@email.com,28,2023-01-15,New York,USA
2,Jane Smith,jane.smith@email.com,32,2023-02-20,London,UK
3,Mike Johnson,mike.johnson@email.com,25,2023-03-10,Toronto,Canada
4,Sarah Wilson,sarah.wilson@email.com,29,2023-04-05,Sydney,Australia
5,David Brown,david.brown@email.com,35,2023-05-12,Berlin,Germany
6,Lisa Davis,lisa.davis@email.com,27,2023-06-18,Tokyo,Japan
7,Tom Anderson,tom.anderson@email.com,31,2023-07-22,Paris,France
8,Emma Taylor,emma.taylor@email.com,26,2023-08-30,Madrid,Spain
9,Chris Miller,chris.miller@email.com,33,2023-09-14,Rome,Italy
10,Anna Garcia,anna.garcia@email.com,24,2023-10-08,Mumbai,India
EOF

# Create sample order data
cat > /opt/spark/sample-data/orders.csv << 'EOF'
order_id,user_id,product_id,quantity,price,order_date,status
1001,1,501,2,29.99,2024-01-15,completed
1002,2,502,1,149.50,2024-01-16,completed
1003,3,503,3,79.99,2024-01-17,pending
1004,1,504,1,199.99,2024-01-18,completed
1005,4,501,5,29.99,2024-01-19,shipped
1006,5,505,2,89.99,2024-01-20,completed
1007,2,506,1,299.99,2024-01-21,pending
1008,6,507,4,19.99,2024-01-22,completed
1009,7,502,2,149.50,2024-01-23,shipped
1010,3,508,1,399.99,2024-01-24,completed
EOF

# Create sample product data
cat > /opt/spark/sample-data/products.csv << 'EOF'
product_id,name,category,price,stock_quantity,brand,description
501,Wireless Headphones,Electronics,29.99,150,TechBrand,High-quality wireless headphones
502,Smart Watch,Electronics,149.50,75,TechBrand,Feature-rich smartwatch
503,Coffee Mug,Home,79.99,200,HomeBrand,Premium ceramic coffee mug
504,Laptop,Electronics,199.99,25,TechBrand,High-performance laptop
505,Running Shoes,Sports,89.99,100,SportsBrand,Comfortable running shoes
506,Gaming Mouse,Electronics,299.99,50,TechBrand,Professional gaming mouse
507,Water Bottle,Sports,19.99,300,SportsBrand,Stainless steel water bottle
508,Smartphone,Electronics,399.99,40,TechBrand,Latest smartphone model
EOF

# Create sample Spark processing script
cat > /opt/spark/sample-data/process_data.py << 'EOF'
from pyspark.sql import SparkSession
from pyspark.sql.functions import *
from pyspark.sql.types import *

# Initialize Spark Session
spark = SparkSession.builder \
    .appName("Database Foundations Analytics") \
    .config("spark.sql.adaptive.enabled", "true") \
    .config("spark.sql.adaptive.coalescePartitions.enabled", "true") \
    .getOrCreate()

# Read CSV files
users_df = spark.read.option("header", "true").option("inferSchema", "true").csv("/opt/spark/sample-data/users.csv")
orders_df = spark.read.option("header", "true").option("inferSchema", "true").csv("/opt/spark/sample-data/orders.csv")
products_df = spark.read.option("header", "true").option("inferSchema", "true").csv("/opt/spark/sample-data/products.csv")

print("=== Data Overview ===")
print("Users count:", users_df.count())
print("Orders count:", orders_df.count())
print("Products count:", products_df.count())

# Join operations
order_details = orders_df.join(users_df, "user_id") \
                        .join(products_df, "product_id")

print("\n=== Order Analysis ===")
# Revenue by user
revenue_by_user = order_details.groupBy("user_id", "name") \
    .agg(sum(col("quantity") * col("price")).alias("total_revenue"),
         count("order_id").alias("order_count")) \
    .orderBy(desc("total_revenue"))

revenue_by_user.show()

# Popular products
popular_products = order_details.groupBy("product_id", "name") \
    .agg(sum("quantity").alias("total_sold"),
         count("order_id").alias("order_count")) \
    .orderBy(desc("total_sold"))

print("\n=== Popular Products ===")
popular_products.show()

# Monthly revenue trend
monthly_revenue = order_details.withColumn("month", date_format("order_date", "yyyy-MM")) \
    .groupBy("month") \
    .agg(sum(col("quantity") * col("price")).alias("monthly_revenue"),
         count("order_id").alias("order_count")) \
    .orderBy("month")

print("\n=== Monthly Revenue Trend ===")
monthly_revenue.show()

# Save results to parquet for further analysis
order_details.write.mode("overwrite").parquet("/opt/spark/output/order_details")
revenue_by_user.write.mode("overwrite").parquet("/opt/spark/output/revenue_by_user")
popular_products.write.mode("overwrite").parquet("/opt/spark/output/popular_products")

print("\n=== Results saved to /opt/spark/output/ ===")

spark.stop()
EOF

echo "Spark sample data and processing script created successfully!"
