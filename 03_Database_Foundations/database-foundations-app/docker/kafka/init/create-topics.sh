#!/bin/bash

# Wait for Kafka to be ready
echo "Waiting for Kafka to start..."
sleep 30

# Create topics for different use cases
echo "Creating Kafka topics..."

# User activity events
kafka-topics --create \
  --bootstrap-server kafka:9092 \
  --topic user-events \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=604800000

# E-commerce order events
kafka-topics --create \
  --bootstrap-server kafka:9092 \
  --topic order-events \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=604800000

# System metrics
kafka-topics --create \
  --bootstrap-server kafka:9092 \
  --topic system-metrics \
  --partitions 2 \
  --replication-factor 1 \
  --config retention.ms=86400000

# Error logs
kafka-topics --create \
  --bootstrap-server kafka:9092 \
  --topic error-logs \
  --partitions 2 \
  --replication-factor 1 \
  --config retention.ms=2592000000

echo "Topics created successfully!"

# Produce sample data
echo "Producing sample data..."

# Sample user events
echo '{"userId": "user123", "action": "login", "timestamp": "2024-01-15T10:00:00Z", "ip": "192.168.1.100"}' | kafka-console-producer --bootstrap-server kafka:9092 --topic user-events
echo '{"userId": "user456", "action": "view_product", "timestamp": "2024-01-15T10:05:00Z", "productId": "prod789"}' | kafka-console-producer --bootstrap-server kafka:9092 --topic user-events
echo '{"userId": "user123", "action": "add_to_cart", "timestamp": "2024-01-15T10:10:00Z", "productId": "prod789", "quantity": 2}' | kafka-console-producer --bootstrap-server kafka:9092 --topic user-events

# Sample order events
echo '{"orderId": "order001", "userId": "user123", "amount": 299.99, "status": "confirmed", "timestamp": "2024-01-15T10:15:00Z"}' | kafka-console-producer --bootstrap-server kafka:9092 --topic order-events
echo '{"orderId": "order002", "userId": "user456", "amount": 149.50, "status": "pending", "timestamp": "2024-01-15T10:20:00Z"}' | kafka-console-producer --bootstrap-server kafka:9092 --topic order-events

# Sample system metrics
echo '{"service": "api-gateway", "cpu": 45.2, "memory": 78.5, "timestamp": "2024-01-15T10:25:00Z"}' | kafka-console-producer --bootstrap-server kafka:9092 --topic system-metrics
echo '{"service": "user-service", "cpu": 32.1, "memory": 65.3, "timestamp": "2024-01-15T10:25:00Z"}' | kafka-console-producer --bootstrap-server kafka:9092 --topic system-metrics

echo "Sample data produced successfully!"

# List topics
echo "Current topics:"
kafka-topics --list --bootstrap-server kafka:9092
