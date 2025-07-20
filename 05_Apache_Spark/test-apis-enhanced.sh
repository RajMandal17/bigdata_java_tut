#!/bin/bash

# Enhanced Test script for Apache Spark Big Data API endpoints
# This script tests all the REST API endpoints in the Spark application

BASE_URL="http://localhost:8080/api/spark"
HEALTH_URL="http://localhost:8080/actuator/health"

echo "==================================="
echo "Apache Spark Big Data API Test Suite"
echo "==================================="

# Function to test an endpoint
test_endpoint() {
    local endpoint=$1
    local description=$2
    echo ""
    echo "Testing: $description"
    echo "Endpoint: $endpoint"
    echo "---"
    
    response=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$endpoint")
    http_code=$(echo "$response" | tail -n1 | sed 's/.*HTTP_CODE://')
    response_body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 200 ]; then
        echo "✅ SUCCESS (HTTP $http_code)"
        echo "$response_body" | python3 -m json.tool 2>/dev/null || echo "$response_body"
    else
        echo "❌ FAILED (HTTP $http_code)"
        echo "$response_body"
    fi
}

# Check if application is running
echo "Checking application health..."
curl -s "$HEALTH_URL" > /dev/null
if [ $? -ne 0 ]; then
    echo "❌ Application is not running on $BASE_URL"
    echo "Please start the application first using: ./start.sh"
    exit 1
fi

echo "✅ Application is running"

# Test Core Spark Operations
echo ""
echo "========================================="
echo "1. TESTING CORE SPARK OPERATIONS"
echo "========================================="

test_endpoint "$BASE_URL/rdd/create" "Create Sample RDD"
test_endpoint "$BASE_URL/rdd/transformations" "RDD Transformations"
test_endpoint "$BASE_URL/rdd/actions" "RDD Actions"
test_endpoint "$BASE_URL/rdd/wordcount" "Word Count"
test_endpoint "$BASE_URL/rdd/joins" "RDD Joins"
test_endpoint "$BASE_URL/rdd/partitions" "RDD Partitions"

test_endpoint "$BASE_URL/dataframe/create" "Create Sample DataFrame"
test_endpoint "$BASE_URL/dataframe/operations" "DataFrame Operations"
test_endpoint "$BASE_URL/dataframe/groupby" "DataFrame GroupBy"
test_endpoint "$BASE_URL/dataframe/joins" "DataFrame Joins"
test_endpoint "$BASE_URL/dataframe/windows" "Window Functions"

test_endpoint "$BASE_URL/sql/create-tables" "Create SQL Tables"
test_endpoint "$BASE_URL/sql/query/simple" "Simple SQL Query"
test_endpoint "$BASE_URL/sql/query/complex" "Complex SQL Query"
test_endpoint "$BASE_URL/sql/aggregations" "SQL Aggregations"

# Test Advanced Operations
echo ""
echo "========================================="
echo "2. TESTING ADVANCED OPERATIONS"
echo "========================================="

test_endpoint "$BASE_URL/advanced/performance-tuning" "Performance Tuning"
test_endpoint "$BASE_URL/advanced/caching" "Caching Strategies"
test_endpoint "$BASE_URL/advanced/partitioning" "Partitioning Analysis"
test_endpoint "$BASE_URL/advanced/broadcast-joins" "Broadcast Joins"

# Test Streaming
echo ""
echo "========================================="
echo "3. TESTING STREAMING OPERATIONS"
echo "========================================="

test_endpoint "$BASE_URL/streaming/start" "Start Streaming (POST)" 
test_endpoint "$BASE_URL/streaming/status" "Streaming Status"
test_endpoint "$BASE_URL/streaming/metrics" "Streaming Metrics"

# Test Machine Learning
echo ""
echo "========================================="
echo "4. TESTING MACHINE LEARNING"
echo "========================================="

test_endpoint "$BASE_URL/ml/linear-regression" "Linear Regression"
test_endpoint "$BASE_URL/ml/classification" "Classification"
test_endpoint "$BASE_URL/ml/clustering" "Clustering"
test_endpoint "$BASE_URL/ml/recommendation" "Recommendation System"
test_endpoint "$BASE_URL/ml/feature-engineering" "Feature Engineering"

# Test GraphX Operations
echo ""
echo "========================================="
echo "5. TESTING GRAPHX OPERATIONS"
echo "========================================="

test_endpoint "$BASE_URL/graph/social-network" "Social Network Graph"
test_endpoint "$BASE_URL/graph/properties" "Graph Properties"
test_endpoint "$BASE_URL/graph/pagerank" "PageRank Algorithm"
test_endpoint "$BASE_URL/graph/connected-components" "Connected Components"
test_endpoint "$BASE_URL/graph/triangles" "Triangle Counting"
test_endpoint "$BASE_URL/graph/citation-network" "Citation Network"

# Test Monitoring
echo ""
echo "========================================="
echo "6. TESTING MONITORING"
echo "========================================="

test_endpoint "$BASE_URL/monitoring/metrics" "Spark Metrics"
test_endpoint "$BASE_URL/monitoring/executors" "Executor Information"
test_endpoint "$BASE_URL/monitoring/jobs" "Job Statistics"
test_endpoint "$BASE_URL/monitoring/stages" "Stage Information"
test_endpoint "$BASE_URL/monitoring/memory" "Memory Usage"
test_endpoint "$BASE_URL/monitoring/performance/test-query" "Query Performance"

# Test Optimization
echo ""
echo "========================================="
echo "7. TESTING OPTIMIZATION"
echo "========================================="

test_endpoint "$BASE_URL/optimization/config" "Configuration Optimization"
test_endpoint "$BASE_URL/optimization/partitioning/test-table" "Partitioning Analysis"
test_endpoint "$BASE_URL/optimization/caching" "Caching Optimization"
test_endpoint "$BASE_URL/optimization/joins" "Join Optimization"
test_endpoint "$BASE_URL/optimization/tuning-guide" "Performance Tuning Guide"

# Summary
echo ""
echo "========================================="
echo "API TESTING COMPLETE"
echo "========================================="
echo ""
echo "📊 Summary:"
echo "• Core Operations: RDD, DataFrame, SQL"
echo "• Advanced Features: Performance tuning, Caching"
echo "• Streaming: Real-time data processing"
echo "• Machine Learning: MLlib algorithms"
echo "• Graph Processing: GraphX operations"
echo "• Monitoring: Application metrics"
echo "• Optimization: Performance recommendations"
echo ""
echo "🔗 Additional Resources:"
echo "• Swagger UI: http://localhost:8080/swagger-ui.html"
echo "• Actuator Health: http://localhost:8080/actuator/health"
echo "• Spark UI: http://localhost:4040"
echo "• Jupyter Notebooks: ./notebooks/"
echo ""
echo "For detailed API documentation, visit the Swagger UI."
