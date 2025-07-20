#!/bin/bash

# API Test Script for Apache Spark Big Data Platform
# This script tests all the major API endpoints

BASE_URL="http://localhost:8090/spark-platform/api/spark"

echo "🧪 Testing Apache Spark Big Data Platform APIs"
echo "================================================"

# Function to test an API endpoint
test_api() {
    local endpoint=$1
    local method=${2:-GET}
    local data=${3:-}
    
    echo "🔍 Testing: $method $endpoint"
    
    if [ "$method" = "POST" ] && [ -n "$data" ]; then
        response=$(curl -s -w "%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "%{http_code}" "$BASE_URL$endpoint")
    fi
    
    http_code="${response: -3}"
    body="${response%???}"
    
    if [ "$http_code" -eq 200 ]; then
        echo "✅ Success: $endpoint"
        echo "   Response: $(echo $body | jq -r '.operation // .success' 2>/dev/null || echo 'OK')"
    else
        echo "❌ Failed: $endpoint (HTTP $http_code)"
        echo "   Response: $body"
    fi
    echo ""
}

# Check if the application is running
echo "🏥 Checking application health..."
test_api "/health"

echo "🔧 Testing RDD Operations..."
test_api "/rdd/basic"
test_api "/rdd/advanced"
test_api "/rdd/partitioning"
test_api "/rdd/caching"

echo "📊 Testing DataFrame Operations..."
test_api "/dataframe/basic"
test_api "/dataframe/advanced"
test_api "/dataset/operations"
test_api "/dataframe/io"

echo "🗃️ Testing SQL Operations..."
test_api "/sql/basic"
test_api "/sql/advanced"
test_api "/sql/udf"
test_api "/sql/optimization"

# Test custom SQL query
echo "🔍 Testing Custom SQL Query..."
custom_query='{"query": "SELECT category, COUNT(*) as count FROM sales GROUP BY category ORDER BY count DESC"}'
test_api "/sql/query" "POST" "$custom_query"

echo "🌊 Testing Streaming Operations..."
test_api "/streaming/status"
test_api "/streaming/metrics"
test_api "/streaming/operations"

echo "🤖 Testing Machine Learning Operations..."
test_api "/ml/feature-engineering"
test_api "/ml/fraud-detection"
test_api "/ml/customer-segmentation"
test_api "/ml/recommendation-system"
test_api "/ml/sales-prediction"
test_api "/ml/operations"

echo "🎉 API Testing Complete!"
echo ""
echo "📊 For more detailed results, check the application logs or use tools like:"
echo "   - Postman collection"
echo "   - Swagger UI: http://localhost:8090/spark-platform/swagger-ui.html"
echo "   - Application metrics: http://localhost:8090/spark-platform/actuator/metrics"
