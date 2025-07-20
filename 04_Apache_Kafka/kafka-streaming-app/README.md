# Apache Kafka Streaming Application

A comprehensive Apache Kafka project demonstrating real-time data streaming, processing, and analytics for Big Data applications using Spring Boot.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Kafka Topics](#kafka-topics)
- [Services](#services)
- [Usage Examples](#usage-examples)
- [Kafka Streams](#kafka-streams)
- [Monitoring](#monitoring)
- [Error Handling](#error-handling)
- [Performance Tuning](#performance-tuning)
- [Curriculum Mapping](#curriculum-mapping)
- [Exercises](#exercises)
- [Assessment Checklist](#assessment-checklist)
- [Troubleshooting](#troubleshooting)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Configuration](#configuration)

## 🎯 Overview

This project implements a production-grade Kafka streaming system that demonstrates:

- **Real-time Event Streaming**: High-throughput message processing with transaction and user events
- **Multiple Consumer Patterns**: Single, batch, conditional, and partition-specific consumers
- **Kafka Streams Processing**: Advanced stream processing with windowing, aggregations, and interactive queries
- **Fraud Detection**: Real-time fraud detection using velocity checks, pattern analysis, and behavioral scoring
- **Error Handling & Retry**: Robust error handling with retry mechanisms and dead letter queues
- **Schema Evolution**: Avro schema support with Schema Registry integration
- **Database Integration**: PostgreSQL for persistent storage and Redis for caching
- **Monitoring & Metrics**: Comprehensive monitoring with Prometheus, Grafana, and custom dashboards
- **Performance Optimization**: Production-ready configuration for high performance and scalability
- **Testing**: Comprehensive test suite including integration tests with TestContainers

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Producers     │───▶│   Kafka Cluster │◄───│   Consumers     │
│                 │    │                 │    │                 │
│ • Transaction   │    │ • Broker        │    │ • Transaction   │
│ • User Events   │    │ • ZooKeeper     │    │ • Analytics     │
│ • Alerts        │    │ • Topics        │    │ • Fraud Detect  │
│ • Analytics     │    │ • Partitions    │    │ • Notifications │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                               │
                       ┌───────┴───────┐
                       │               │
              ┌─────────────────┐ ┌─────────────────┐
              │ Kafka Streams   │ │   Schema        │
              │   Processing    │ │   Registry      │
              └─────────────────┘ └─────────────────┘
                       │
         ┌─────────────┼─────────────┐
         │             │             │
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│   PostgreSQL    │ │     Redis       │ │ Elasticsearch   │
│  (Persistence)  │ │   (Caching)     │ │   (Search)      │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

## 📋 Prerequisites

- Docker and Docker Compose
- Java 17 or higher
- Maven 3.6+
- 8GB+ RAM recommended
- 20GB+ free disk space

## 🚀 Quick Start

### Option 1: Automated Setup (Recommended)

```bash
# Clone or navigate to the project directory
cd kafka-streaming-app

# Start everything with a single command
./start.sh

# Check system status
./start.sh status

# Generate sample data for testing
./start.sh sample

# View application logs
./start.sh logs

# Stop all services
./start.sh stop
```

### Option 2: Manual Setup

1. **Start Infrastructure:**
   ```bash
   docker-compose up -d
   ```

2. **Build Application:**
   ```bash
   mvn clean package
   ```

3. **Run Tests:**
   ```bash
   mvn test
   ```

4. **Start Application:**
   ```bash
   java -jar target/kafka-streaming-app-*.jar
   ```

## 📊 New Features and Components

### Kafka Streams Processing
- **Transaction Analytics**: Real-time transaction aggregation and windowing
- **Fraud Detection**: Pattern-based fraud detection with velocity checks
- **Customer Spending**: Real-time customer spending analytics
- **Interactive Queries**: Query live streaming data via REST APIs

### Advanced Error Handling
- **Dead Letter Queues**: Automatic routing of failed messages
- **Retry Mechanisms**: Configurable retry strategies with exponential backoff
- **Error Classification**: Automatic categorization of recoverable vs non-recoverable errors
- **Manual Recovery**: Tools for manual intervention and message replay

### Business Logic Integration
- **Transaction Processing**: Complete transaction lifecycle management
- **Fraud Detection Service**: Real-time fraud detection with multiple algorithms
- **Notification Service**: Multi-channel notifications (email, SMS, push)
- **Database Persistence**: Robust data persistence with PostgreSQL

### REST API Endpoints
- **Event Production**: APIs for sending transactions, user events, and alerts
- **Analytics**: Real-time analytics and reporting endpoints
- **Monitoring**: Health checks and system status endpoints
- **Management**: Dead letter queue management and manual recovery

### Comprehensive Testing
- **Integration Tests**: Full end-to-end testing with embedded Kafka
- **Unit Tests**: Component-level testing with mocking
- **Performance Tests**: Load testing and performance benchmarking
- **Contract Tests**: API contract testing and validation

## 📊 Services and Access Points

### Kafka Infrastructure
- **Kafka Broker**: localhost:9092
- **ZooKeeper**: localhost:2181
- **Kafka UI**: http://localhost:8080
- **Schema Registry**: http://localhost:8081
- **Kafka Connect**: http://localhost:8083
- **KSQL Server**: http://localhost:8088

### Supporting Services
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379
- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000

### Application
- **Spring Boot App**: http://localhost:8090
- **Health Check**: http://localhost:8090/actuator/health
- **Metrics**: http://localhost:8090/actuator/metrics
- **Prometheus Metrics**: http://localhost:8090/actuator/prometheus

## 🎯 Kafka Topics

| Topic | Partitions | Purpose | Message Type |
|-------|------------|---------|--------------|
| `transactions-topic` | 3 | Financial transactions | TransactionEvent |
| `user-events-topic` | 3 | User activity events | UserEvent |
| `alerts-topic` | 2 | System alerts | AlertEvent |
| `analytics-topic` | 3 | Analytics data | AnalyticsEvent |
| `notifications-topic` | 2 | User notifications | NotificationEvent |
| `fraud-detection-topic` | 2 | Fraud alerts | FraudEvent |
| `system-metrics-topic` | 2 | System metrics | MetricsEvent |

## 📱 Usage Examples

### 1. Producing Messages

#### Send Transaction Event
```bash
curl -X POST http://localhost:8090/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "cust123",
    "amount": 99.99,
    "currency": "USD",
    "category": "groceries",
    "merchantName": "SuperMart"
  }'
```

#### Send User Event
```bash
curl -X POST http://localhost:8090/api/events/user \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user456",
    "eventType": "page_view",
    "data": {
      "page": "/products/123",
      "duration": 45000
    }
  }'
```

#### Send Alert
```bash
curl -X POST http://localhost:8090/api/alerts \
  -H "Content-Type: application/json" \
  -d '{
    "alertType": "high_cpu",
    "severity": "HIGH",
    "description": "CPU usage above 80%",
    "data": {
      "service": "payment-service",
      "cpu_percent": 85.5
    }
  }'
```

### 2. Consumer Examples

#### Check Consumer Group Status
```bash
# List consumer groups
kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Describe consumer group
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group transaction-processors
```

#### Monitor Topic Messages
```bash
# Consume from beginning
kafka-console-consumer --bootstrap-server localhost:9092 --topic transactions-topic --from-beginning

# Consume with key
kafka-console-consumer --bootstrap-server localhost:9092 --topic transactions-topic --property print.key=true --property key.separator=":"
```

### 3. Performance Testing

#### High-Volume Producer Test
```bash
# Generate test data
kafka-producer-perf-test --topic transactions-topic \
  --num-records 100000 \
  --record-size 1024 \
  --throughput 10000 \
  --producer-props bootstrap.servers=localhost:9092
```

#### Consumer Performance Test
```bash
# Test consumer performance
kafka-consumer-perf-test --topic transactions-topic \
  --bootstrap-server localhost:9092 \
  --messages 100000 \
  --group perf-consumer-group
```

## 🔄 Kafka Streams Processing

### Stream Processing Topologies

1. **Transaction Enrichment Stream**
   - Input: Raw transactions
   - Processing: Enrich with customer data
   - Output: Enriched transactions

2. **Fraud Detection Stream**
   - Input: Transaction events
   - Processing: Windowed aggregation, anomaly detection
   - Output: Fraud alerts

3. **Real-time Analytics Stream**
   - Input: User events
   - Processing: Real-time metrics calculation
   - Output: Analytics events

### Interactive Queries

```bash
# Query customer spending (last hour)
curl http://localhost:8090/api/streams/customer-spending/cust123

# Query fraud patterns
curl http://localhost:8090/api/streams/fraud-patterns?window=1h

# Query user behavior
curl http://localhost:8090/api/streams/user-behavior/user456
```

## 📊 Monitoring and Metrics

### Kafka UI Dashboard
- Open http://localhost:8080
- Monitor topics, partitions, consumer groups
- View message rates and lag

### Prometheus Metrics
```bash
# Application metrics
curl http://localhost:8090/actuator/prometheus

# Kafka metrics
curl http://localhost:9090/api/v1/query?query=kafka_consumer_lag_sum
```

### Grafana Dashboards
1. Open http://localhost:3000 (admin/admin)
2. Import Kafka dashboard
3. Monitor:
   - Message throughput
   - Consumer lag
   - Error rates
   - Processing latency

### Custom Metrics Examples

```java
// In your service class
@Autowired
private MeterRegistry meterRegistry;

// Counter for processed messages
Counter.builder("kafka.messages.processed")
       .tag("topic", "transactions")
       .register(meterRegistry)
       .increment();

// Timer for processing duration
Timer.Sample sample = Timer.start(meterRegistry);
// ... processing logic ...
sample.stop(Timer.builder("kafka.processing.time")
            .tag("consumer", "transaction-processor")
            .register(meterRegistry));
```

## ⚠️ Error Handling

### Retry Configuration

```properties
# Retry settings
retry.max-attempts=3
retry.delay=1000
retry.multiplier=2
```

### Dead Letter Queue Processing

```bash
# Check DLQ topics
kafka-topics --bootstrap-server localhost:9092 --list | grep dlq

# Consume from DLQ
kafka-console-consumer --bootstrap-server localhost:9092 --topic transactions-topic-dlq --from-beginning
```

### Error Monitoring

```bash
# Monitor error rates
curl http://localhost:8090/actuator/metrics/kafka.consumer.errors

# Check failed messages
curl http://localhost:8090/api/errors/failed-messages
```

## ⚡ Performance Tuning

### Producer Optimization

```properties
# High throughput settings
spring.kafka.producer.batch-size=65536
spring.kafka.producer.linger-ms=20
spring.kafka.producer.compression-type=lz4
spring.kafka.producer.buffer-memory=67108864
```

### Consumer Optimization

```properties
# High throughput settings
spring.kafka.consumer.max-poll-records=1000
spring.kafka.consumer.fetch-min-size=50000
spring.kafka.consumer.fetch-max-wait=500ms
```

### JVM Tuning

```bash
# Set JVM options
export JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=20"
```

## 📚 Curriculum Mapping

| Topic | Implementation | Location |
|-------|---------------|----------|
| Kafka Fundamentals | Producer/Consumer setup | `config/KafkaConfig.java` |
| Message Production | Producer service | `producer/KafkaProducerService.java` |
| Message Consumption | Consumer services | `consumer/*.java` |
| Stream Processing | Kafka Streams | `streams/` |
| Error Handling | Retry & DLQ | `error/` |
| Schema Evolution | Avro schemas | `src/main/avro/` |
| Monitoring | Metrics & Health | `actuator/` |
| Performance | Optimization configs | `application.properties` |

## 🎯 Practical Exercises

### Exercise 1: Basic Producer/Consumer
1. Create a new topic for order events
2. Implement OrderEvent model class
3. Create producer and consumer for order processing
4. Add proper error handling and logging

### Exercise 2: Stream Processing
1. Implement a Kafka Streams topology for real-time inventory updates
2. Create windowed aggregations for hourly sales reports
3. Add interactive queries for real-time dashboard

### Exercise 3: Advanced Patterns
1. Implement exactly-once processing semantics
2. Create a multi-topic consumer with different processing logic
3. Add custom partitioning strategy based on geography

### Exercise 4: Production Deployment
1. Configure Kafka cluster for high availability
2. Implement monitoring and alerting
3. Set up automated scaling based on consumer lag

### Exercise 5: Integration Testing
1. Create integration tests with TestContainers
2. Test producer/consumer interactions
3. Validate error handling scenarios

## ✅ Assessment Checklist

- [ ] **Kafka Architecture**: Understand brokers, topics, partitions, and consumer groups
- [ ] **Message Production**: Can implement reliable message producers with proper serialization
- [ ] **Message Consumption**: Can create various consumer patterns (single, batch, conditional)
- [ ] **Stream Processing**: Can build Kafka Streams applications with aggregations and joins
- [ ] **Error Handling**: Implements retry mechanisms, dead letter queues, and error monitoring
- [ ] **Performance**: Understands throughput optimization and latency reduction techniques
- [ ] **Monitoring**: Can set up comprehensive monitoring and alerting
- [ ] **Schema Evolution**: Understands schema management and backward compatibility
- [ ] **Production Readiness**: Can configure Kafka for production environments
- [ ] **Integration**: Can integrate Kafka with other systems and databases

## 🔧 Troubleshooting

### Common Issues

**1. Kafka not starting:**
```bash
# Check logs
docker-compose logs kafka

# Verify ZooKeeper is running
docker-compose logs zookeeper

# Restart services
docker-compose restart kafka
```

**2. Consumer lag issues:**
```bash
# Check consumer group status
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group your-group

# Reset offsets if needed
kafka-consumer-groups --bootstrap-server localhost:9092 --group your-group --reset-offsets --to-earliest --topic your-topic --execute
```

**3. Message serialization errors:**
```bash
# Check topic configuration
kafka-topics --bootstrap-server localhost:9092 --describe --topic your-topic

# Verify message format
kafka-console-consumer --bootstrap-server localhost:9092 --topic your-topic --property print.key=true
```

**4. Performance issues:**
```bash
# Monitor JVM metrics
jstat -gc [PID] 1s

# Check Kafka metrics
curl http://localhost:8090/actuator/metrics/kafka.producer.batch-size-avg
```

### Memory and Resource Requirements

- **Minimum**: 8GB RAM, 4 CPU cores
- **Recommended**: 16GB RAM, 8 CPU cores
- **Production**: 32GB+ RAM, 16+ CPU cores

### Port Conflicts

If ports are in use, modify `docker-compose.yml`:
```yaml
services:
  kafka:
    ports:
      - "9093:9092"  # Change external port
```

## 📝 Next Steps

After completing this module:

1. **Apache Spark Integration** (Module 05)
2. **Real-time Analytics with Stream Processing**
3. **Microservices with Event-Driven Architecture**
4. **Cloud Deployment (Confluent Cloud/AWS MSK)**

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Happy Streaming!** 🚀

For questions or support, please create an issue in the repository.

## 🔗 API Endpoints

### Event Production APIs

#### Send Transaction Event
```bash
curl -X POST http://localhost:8080/api/events/transaction \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "amount": 150.75,
    "category": "GROCERY",
    "metadata": {"location": "Store123"}
  }'
```

#### Send User Event
```bash
curl -X POST http://localhost:8080/api/events/user \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "USER001",
    "eventType": "LOGIN",
    "sessionId": "session123",
    "data": {"device": "mobile"}
  }'
```

#### Send Alert
```bash
curl -X POST http://localhost:8080/api/events/alert \
  -H "Content-Type: application/json" \
  -d '{
    "alertType": "FRAUD_DETECTED",
    "data": {"transactionId": "TXN123", "riskScore": 0.95}
  }'
```

#### Generate Sample Data
```bash
curl -X POST "http://localhost:8080/api/events/generate/sample?transactionCount=100&userEventCount=50"
```

### Analytics APIs

#### Get Customer Spending
```bash
curl "http://localhost:8080/api/analytics/customer/CUST001/spending?hours=24"
```

#### Get Transaction Counts
```bash
curl "http://localhost:8080/api/analytics/customer/CUST001/transactions?hours=1"
```

#### Get Top Spenders
```bash
curl "http://localhost:8080/api/analytics/top-spenders?limit=10"
```

#### Get Analytics Dashboard
```bash
curl "http://localhost:8080/api/analytics/dashboard"
```

#### Get Fraud Statistics
```bash
curl "http://localhost:8080/api/analytics/fraud/statistics"
```

### Management APIs

#### Get Failed Messages
```bash
curl "http://localhost:8080/api/analytics/failed-messages?limit=20"
```

#### Mark Failed Message as Resolved
```bash
curl -X PUT "http://localhost:8080/api/analytics/failed-messages/123/resolve"
```

#### Health Check
```bash
curl "http://localhost:8080/api/events/health"
curl "http://localhost:8080/api/analytics/health"
```

## 📁 Project Structure

```
kafka-streaming-app/
├── src/
│   ├── main/
│   │   ├── java/com/bigdata/kafka/
│   │   │   ├── KafkaStreamingApplication.java      # Main application class
│   │   │   ├── config/
│   │   │   │   └── KafkaConfig.java                # Kafka configuration
│   │   │   ├── model/                              # Data models
│   │   │   │   ├── TransactionEvent.java
│   │   │   │   ├── UserEvent.java
│   │   │   │   └── AlertEvent.java
│   │   │   ├── producer/
│   │   │   │   └── KafkaProducerService.java       # Message production
│   │   │   ├── consumer/                           # Message consumption
│   │   │   │   ├── TransactionEventConsumer.java
│   │   │   │   └── UserEventConsumer.java
│   │   │   ├── streams/                            # Stream processing
│   │   │   │   ├── KafkaStreamsConfig.java
│   │   │   │   ├── TransactionStreamsProcessor.java
│   │   │   │   └── InteractiveQueriesService.java
│   │   │   ├── service/                            # Business logic
│   │   │   │   ├── TransactionProcessingService.java
│   │   │   │   ├── FraudDetectionService.java
│   │   │   │   └── NotificationService.java
│   │   │   ├── controller/                         # REST APIs
│   │   │   │   ├── EventProducerController.java
│   │   │   │   └── AnalyticsController.java
│   │   │   └── error/                              # Error handling
│   │   │       ├── KafkaConsumerErrorHandler.java
│   │   │       ├── DeadLetterQueueProcessor.java
│   │   │       ├── DeadLetterService.java
│   │   │       └── RetryService.java
│   │   └── resources/
│   │       ├── application.properties              # Configuration
│   │       └── db/
│   │           └── schema.sql                      # Database schema
│   └── test/
│       └── java/com/bigdata/kafka/
│           └── KafkaIntegrationTest.java           # Integration tests
├── monitoring/                                     # Monitoring configuration
│   ├── prometheus.yml
│   ├── kafka_alerts.yml
│   └── grafana-dashboard.json
├── docker-compose.yml                              # Infrastructure
├── pom.xml                                         # Maven dependencies
├── start.sh                                        # Startup script
└── README.md                                       # Documentation
```

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=KafkaIntegrationTest

# Run tests with coverage
mvn test jacoco:report

# Skip tests during build
mvn clean package -DskipTests
```

### Test Coverage

- **Integration Tests**: End-to-end testing with embedded Kafka
- **Unit Tests**: Service layer and business logic testing
- **Consumer Tests**: Message consumption and processing
- **Producer Tests**: Message production and serialization
- **Streams Tests**: Stream processing topology testing
- **Error Handling Tests**: Error scenarios and recovery

### Test Data

The project includes utilities for generating test data:

```bash
# Generate sample transactions and user events
curl -X POST "http://localhost:8080/api/events/generate/sample?transactionCount=100&userEventCount=50"
```

## 🔧 Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.acks=1
spring.kafka.producer.retries=3
spring.kafka.consumer.group-id=bigdata-group
spring.kafka.consumer.auto-offset-reset=earliest

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/kafkadb
spring.datasource.username=kafkauser
spring.datasource.password=kafkapass

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379

# Monitoring Configuration
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true

# Custom Topic Configuration
kafka.topics.transactions=transactions-topic
kafka.topics.user-events=user-events-topic
kafka.topics.alerts=alerts-topic
```

### Environment Variables

Override configuration using environment variables:

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export POSTGRES_URL=jdbc:postgresql://localhost:5432/kafkadb
export REDIS_HOST=localhost
export LOG_LEVEL=INFO
```
