#!/bin/bash

# Wait for Elasticsearch to be ready
echo "Waiting for Elasticsearch to start..."
until curl -s "http://elasticsearch:9200/_cluster/health" | grep -q '"status":"green\|yellow"'; do
  echo "Waiting for Elasticsearch..."
  sleep 10
done

echo "Elasticsearch is ready. Creating indexes and sample data..."

# Create product catalog index
curl -X PUT "http://elasticsearch:9200/products" \
  -H "Content-Type: application/json" \
  -d '{
    "mappings": {
      "properties": {
        "product_id": {"type": "keyword"},
        "name": {"type": "text", "analyzer": "standard"},
        "description": {"type": "text", "analyzer": "standard"},
        "category": {"type": "keyword"},
        "brand": {"type": "keyword"},
        "price": {"type": "double"},
        "stock_quantity": {"type": "integer"},
        "tags": {"type": "keyword"},
        "created_at": {"type": "date"},
        "updated_at": {"type": "date"}
      }
    },
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0
    }
  }'

# Create user activity logs index
curl -X PUT "http://elasticsearch:9200/user-activity" \
  -H "Content-Type: application/json" \
  -d '{
    "mappings": {
      "properties": {
        "user_id": {"type": "keyword"},
        "session_id": {"type": "keyword"},
        "action": {"type": "keyword"},
        "page": {"type": "keyword"},
        "timestamp": {"type": "date"},
        "ip_address": {"type": "ip"},
        "user_agent": {"type": "text"},
        "duration": {"type": "integer"},
        "metadata": {"type": "object"}
      }
    },
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0
    }
  }'

# Create system logs index
curl -X PUT "http://elasticsearch:9200/system-logs" \
  -H "Content-Type: application/json" \
  -d '{
    "mappings": {
      "properties": {
        "service": {"type": "keyword"},
        "level": {"type": "keyword"},
        "message": {"type": "text"},
        "timestamp": {"type": "date"},
        "host": {"type": "keyword"},
        "environment": {"type": "keyword"},
        "error_code": {"type": "keyword"},
        "stack_trace": {"type": "text"}
      }
    },
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0
    }
  }'

echo "Indexes created. Adding sample data..."

# Sample product data
curl -X POST "http://elasticsearch:9200/products/_doc/1" \
  -H "Content-Type: application/json" \
  -d '{
    "product_id": "TECH001",
    "name": "Wireless Bluetooth Headphones",
    "description": "High-quality wireless headphones with noise cancellation",
    "category": "Electronics",
    "brand": "TechBrand",
    "price": 99.99,
    "stock_quantity": 50,
    "tags": ["wireless", "bluetooth", "headphones", "audio"],
    "created_at": "2024-01-15T10:00:00Z",
    "updated_at": "2024-01-15T10:00:00Z"
  }'

curl -X POST "http://elasticsearch:9200/products/_doc/2" \
  -H "Content-Type: application/json" \
  -d '{
    "product_id": "TECH002",
    "name": "Smart Fitness Watch",
    "description": "Advanced fitness tracking with heart rate monitor",
    "category": "Electronics",
    "brand": "FitBrand",
    "price": 249.99,
    "stock_quantity": 30,
    "tags": ["smartwatch", "fitness", "health", "wearable"],
    "created_at": "2024-01-15T10:00:00Z",
    "updated_at": "2024-01-15T10:00:00Z"
  }'

curl -X POST "http://elasticsearch:9200/products/_doc/3" \
  -H "Content-Type: application/json" \
  -d '{
    "product_id": "HOME001",
    "name": "Premium Coffee Maker",
    "description": "Programmable coffee maker with thermal carafe",
    "category": "Home",
    "brand": "BrewMaster",
    "price": 179.99,
    "stock_quantity": 25,
    "tags": ["coffee", "kitchen", "appliance", "thermal"],
    "created_at": "2024-01-15T10:00:00Z",
    "updated_at": "2024-01-15T10:00:00Z"
  }'

# Sample user activity data
curl -X POST "http://elasticsearch:9200/user-activity/_doc/1" \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "user123",
    "session_id": "sess_abc123",
    "action": "page_view",
    "page": "/products/TECH001",
    "timestamp": "2024-01-15T10:30:00Z",
    "ip_address": "192.168.1.100",
    "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "duration": 45,
    "metadata": {"referrer": "https://google.com", "device": "desktop"}
  }'

curl -X POST "http://elasticsearch:9200/user-activity/_doc/2" \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "user456",
    "session_id": "sess_def456",
    "action": "search",
    "page": "/search",
    "timestamp": "2024-01-15T10:35:00Z",
    "ip_address": "192.168.1.101",
    "user_agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)",
    "duration": 15,
    "metadata": {"query": "wireless headphones", "results_count": 12, "device": "mobile"}
  }'

# Sample system logs
curl -X POST "http://elasticsearch:9200/system-logs/_doc/1" \
  -H "Content-Type: application/json" \
  -d '{
    "service": "user-service",
    "level": "INFO",
    "message": "User authentication successful",
    "timestamp": "2024-01-15T10:40:00Z",
    "host": "user-service-01",
    "environment": "production",
    "error_code": null
  }'

curl -X POST "http://elasticsearch:9200/system-logs/_doc/2" \
  -H "Content-Type: application/json" \
  -d '{
    "service": "payment-service",
    "level": "ERROR",
    "message": "Payment processing failed",
    "timestamp": "2024-01-15T10:45:00Z",
    "host": "payment-service-02",
    "environment": "production",
    "error_code": "PAY001",
    "stack_trace": "java.lang.Exception: Payment gateway timeout\n\tat com.example.PaymentService.processPayment(PaymentService.java:45)"
  }'

echo "Sample data added to Elasticsearch!"

# Create index templates for future data
curl -X PUT "http://elasticsearch:9200/_index_template/logs-template" \
  -H "Content-Type: application/json" \
  -d '{
    "index_patterns": ["logs-*"],
    "template": {
      "mappings": {
        "properties": {
          "@timestamp": {"type": "date"},
          "service": {"type": "keyword"},
          "level": {"type": "keyword"},
          "message": {"type": "text"},
          "host": {"type": "keyword"}
        }
      },
      "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0
      }
    }
  }'

echo "Elasticsearch initialization completed!"
