# Apache Spark Big Data Platform

A comprehensive, production-grade Apache Spark application built with Spring Boot, demonstrating the full spectrum of Spark capabilities including RDD operations, DataFrame/Dataset processing, Spark SQL, Structured Streaming, and Machine Learning.

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Web Layer (REST APIs)                    │
├─────────────────────────────────────────────────────────────┤
│                    Service Layer                           │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐ │
│  │     RDD     │ DataFrame/  │    SQL      │     ML      │ │
│  │  Operations │  Dataset    │ Operations  │  Pipeline   │ │
│  └─────────────┴─────────────┴─────────────┴─────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                 Apache Spark Core                          │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐ │
│  │ Spark Core  │ Spark SQL   │  Streaming  │    MLlib    │ │
│  └─────────────┴─────────────┴─────────────┴─────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                    Data Sources                            │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐ │
│  │   PostgreSQL│    Kafka    │    Files    │   Memory    │ │
│  └─────────────┴─────────────┴─────────────┴─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Features

### Core Spark Operations
- **RDD Operations**: Transformations, actions, partitioning, caching
- **DataFrame/Dataset**: Structured data processing with type safety
- **Spark SQL**: Complex queries, UDFs, optimization techniques
- **Performance Optimization**: Caching, partitioning, broadcast joins

### Streaming Analytics
- **Structured Streaming**: Real-time data processing from Kafka
- **Fraud Detection**: Real-time anomaly detection
- **Session Analytics**: User behavior analysis
- **Metrics Dashboard**: Live streaming metrics

### Machine Learning
- **Feature Engineering**: Comprehensive ML pipeline
- **Classification**: Fraud detection with multiple algorithms
- **Clustering**: Customer segmentation using K-means
- **Recommendation**: Collaborative filtering with ALS
- **Regression**: Sales prediction modeling

### Production Features
- **Docker Support**: Complete containerized deployment
- **Monitoring**: Prometheus metrics and Grafana dashboards
- **REST APIs**: Comprehensive API endpoints
- **Configuration**: Multi-environment support
- **Logging**: Structured logging with different levels

## 📋 Prerequisites

- Java 11 or higher
- Maven 3.6+
- Docker and Docker Compose
- 8GB+ RAM recommended

## 🛠️ Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd 05_Apache_Spark
mvn clean package -DskipTests
```

### 2. Start Infrastructure

```bash
# Start all services (Spark cluster, Kafka, PostgreSQL, monitoring)
docker-compose up -d

# Wait for services to be ready (check logs)
docker-compose logs -f spark-master
```

### 3. Run Application

```bash
# Run with local Spark
mvn spring-boot:run

# Or run with Docker Spark cluster
mvn spring-boot:run -Dspring.profiles.active=docker
```

### 4. Access Services

- **Application API**: http://localhost:8090/spark-platform
- **Spark Master UI**: http://localhost:8080
- **Spark History Server**: http://localhost:18080
- **Grafana Dashboard**: http://localhost:3000 (admin/admin)
- **Prometheus Metrics**: http://localhost:9090
- **Jupyter Notebook**: http://localhost:8888

## 📚 API Documentation

### Core Spark Operations

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/spark/health` | GET | Health check |
| `/api/spark/rdd/basic` | GET | Basic RDD operations |
| `/api/spark/rdd/advanced` | GET | Advanced RDD operations |
| `/api/spark/rdd/partitioning` | GET | RDD partitioning demo |
| `/api/spark/rdd/caching` | GET | RDD caching demo |
| `/api/spark/dataframe/basic` | GET | Basic DataFrame operations |
| `/api/spark/dataframe/advanced` | GET | Advanced DataFrame operations |
| `/api/spark/dataset/operations` | GET | Dataset operations |
| `/api/spark/dataframe/io` | GET | DataFrame I/O operations |
| `/api/spark/sql/basic` | GET | Basic SQL operations |
| `/api/spark/sql/advanced` | GET | Advanced SQL operations |
| `/api/spark/sql/udf` | GET | User Defined Functions |
| `/api/spark/sql/optimization` | GET | SQL optimization |
| `/api/spark/sql/query` | POST | Execute custom SQL |

### Streaming Operations

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/spark/streaming/metrics` | GET | Current streaming metrics |
| `/api/spark/streaming/status` | GET | Streaming query status |
| `/api/spark/streaming/start/events` | POST | Start event processing |
| `/api/spark/streaming/start/fraud` | POST | Start fraud detection |
| `/api/spark/streaming/start/analytics` | POST | Start real-time analytics |
| `/api/spark/streaming/stop/{queryName}` | POST | Stop specific query |
| `/api/spark/streaming/stop/all` | POST | Stop all queries |

### Machine Learning

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/spark/ml/feature-engineering` | GET | Feature engineering demo |
| `/api/spark/ml/fraud-detection` | GET | ML fraud detection |
| `/api/spark/ml/customer-segmentation` | GET | Customer clustering |
| `/api/spark/ml/recommendation-system` | GET | Recommendation engine |
| `/api/spark/ml/sales-prediction` | GET | Sales prediction model |

## 🔧 Configuration

### Environment Profiles

- **development**: Local development with embedded Spark
- **docker**: Docker environment with Spark cluster
- **production**: Production-ready configuration

### Key Configuration Properties

```yaml
# Spark Configuration
spark:
  master: "local[*]"  # or "spark://spark-master:7077"
  app.name: "BigData-Spark-Platform"
  
# Kafka Configuration
kafka:
  bootstrap.servers: localhost:9092
  topic:
    events: events-topic
    transactions: transactions-topic
    
# Streaming Configuration
streaming:
  checkpoint.location: /tmp/spark-streaming-checkpoint
  batch.interval: 10s
```

## 🚀 Usage Examples

### 1. Basic RDD Operations

```bash
curl -X GET http://localhost:8090/spark-platform/api/spark/rdd/basic
```

Response:
```json
{
  "success": true,
  "operation": "basic_rdd_operations",
  "data": {
    "original_count": 10,
    "even_numbers": [2, 4, 6, 8, 10],
    "squares": [4, 16, 36, 64, 100],
    "sum_of_squares": 220,
    "count": 5
  }
}
```

### 2. Advanced DataFrame Operations

```bash
curl -X GET http://localhost:8090/spark-platform/api/spark/dataframe/advanced
```

### 3. Custom SQL Query

```bash
curl -X POST http://localhost:8090/spark-platform/api/spark/sql/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "SELECT category, COUNT(*) as count, SUM(total_amount) as total FROM sales GROUP BY category ORDER BY total DESC"
  }'
```

### 4. Start Streaming Analytics

```bash
curl -X POST http://localhost:8090/spark-platform/api/spark/streaming/start/analytics
```

### 5. Run Machine Learning

```bash
curl -X GET http://localhost:8090/spark-platform/api/spark/ml/customer-segmentation
```

## 📊 Sample Data Generation

The application includes built-in sample data generators for:

- **Sales Transactions**: Customer purchases with demographics
- **Streaming Events**: User activity events
- **Fraud Data**: Synthetic fraud detection dataset
- **Customer Data**: Customer profiles and behavior
- **Product Ratings**: User-item ratings for recommendations

## 🔍 Monitoring and Observability

### Metrics Available

- **Application Metrics**: Request rates, response times, error rates
- **Spark Metrics**: Job execution times, memory usage, CPU utilization
- **Streaming Metrics**: Processing rates, batch durations, watermarks
- **ML Metrics**: Model accuracy, training times, prediction latencies

### Grafana Dashboards

Pre-configured dashboards for:
- Spark Application Overview
- Streaming Analytics
- Machine Learning Models
- System Resources

### Prometheus Queries

Example queries:
```promql
# Average request duration
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Spark job success rate
rate(spark_job_success_total[5m]) / rate(spark_job_total[5m])

# Streaming batch processing time
spark_streaming_batch_processing_time_seconds
```

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn test -Dtest=**/*IntegrationTest
```

### Performance Tests

```bash
mvn test -Dtest=**/*PerformanceTest
```

## 🚀 Deployment

### Local Development

```bash
# Start infrastructure
docker-compose up -d

# Run application
mvn spring-boot:run
```

### Docker Deployment

```bash
# Build application image
mvn clean package
docker build -t spark-platform:latest .

# Run with Docker Compose
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Kubernetes Deployment

```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/
```

## 📈 Performance Tuning

### Spark Configuration

```yaml
spark:
  executor:
    memory: 4g
    cores: 2
  driver:
    memory: 2g
  sql:
    adaptive:
      enabled: true
      coalescePartitions.enabled: true
      skewJoin.enabled: true
```

### JVM Tuning

```bash
JAVA_OPTS="-Xmx8g -Xms4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

## 🔒 Security

### Production Security Checklist

- [ ] Enable authentication for Spark UI
- [ ] Configure SSL/TLS for all communications
- [ ] Set up proper network security groups
- [ ] Enable audit logging
- [ ] Configure access controls for APIs
- [ ] Use secrets management for credentials

## 🐛 Troubleshooting

### Common Issues

1. **OutOfMemoryError**
   ```bash
   # Increase driver memory
   spark.driver.memory=4g
   spark.driver.maxResultSize=2g
   ```

2. **Streaming Query Fails**
   ```bash
   # Check checkpoint directory permissions
   ls -la /tmp/spark-streaming-checkpoint
   ```

3. **Kafka Connection Issues**
   ```bash
   # Verify Kafka is accessible
   docker-compose logs kafka
   ```

### Debug Mode

```bash
# Enable debug logging
mvn spring-boot:run -Dlogging.level.com.bigdata.spark=DEBUG
```

## 📖 Learning Path

### Beginner
1. Start with basic RDD operations
2. Explore DataFrame operations
3. Try simple SQL queries
4. Run basic ML examples

### Intermediate
1. Advanced DataFrame operations
2. Complex SQL with window functions
3. Streaming analytics
4. Feature engineering

### Advanced
1. Performance optimization
2. Custom UDFs and UDAFs
3. Advanced ML pipelines
4. Production deployment

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Apache Spark community
- Spring Boot team
- Contributors and testers

## 📞 Support

For questions and support:
- Create an issue in the repository
- Check the troubleshooting section
- Review the API documentation

---

**Happy Big Data Processing with Apache Spark! 🎉**
